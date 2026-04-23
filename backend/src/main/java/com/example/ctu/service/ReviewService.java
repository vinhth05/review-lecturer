package com.example.ctu.service;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Review;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ForbiddenException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReportRepository;
import com.example.ctu.repository.ReviewRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@SuppressWarnings("null")
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final LecturerRepository lecturerRepository;
    private final CurrentUserService currentUserService;
    private final HashService hashService;
    private final ToxicFilterService toxicFilterService;
    private final AppPropertiesFacade appPropertiesFacade;
    private final SimpMessagingTemplate messagingTemplate;

    public ReviewService(ReviewRepository reviewRepository,
                         ReportRepository reportRepository,
                         LecturerRepository lecturerRepository,
                         CurrentUserService currentUserService,
                         HashService hashService,
                         ToxicFilterService toxicFilterService,
                         AppPropertiesFacade appPropertiesFacade,
                         SimpMessagingTemplate messagingTemplate) {
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.lecturerRepository = lecturerRepository;
        this.currentUserService = currentUserService;
        this.hashService = hashService;
        this.toxicFilterService = toxicFilterService;
        this.appPropertiesFacade = appPropertiesFacade;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Review submit(ReviewDtos.CreateReviewRequest request) {
        User user = requireVerifiedStudent();
        String anonymousHash = hashService.anonymousHash(user.getStudentCode());
        if (reviewRepository.countByAnonymousHashAndCreatedAtBetween(anonymousHash, startOfDay(), endOfDay()) >= appPropertiesFacade.reviewRateLimitPerDay()) {
            throw new BadRequestException("Vượt quá giới hạn 5 review mỗi ngày");
        }
        if (reviewRepository.existsByAnonymousHashAndLecturer_IdAndSemesterAndAcademicYear(
                anonymousHash, request.lecturerId(), request.semester(), request.academicYear())) {
            throw new BadRequestException("Bạn đã review giảng viên này trong học kỳ/năm học này");
        }
        if (toxicFilterService.containsToxicWord(request.comment())) {
            throw new BadRequestException("Nội dung có từ khóa không phù hợp");
        }
        Lecturer lecturer = lecturerRepository.findById(request.lecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        if (lecturer.getStatus() == LecturerStatus.HIDDEN) {
            throw new ForbiddenException("Giảng viên đang bị ẩn");
        }
        Review review = Review.builder()
                .lecturer(lecturer)
                .anonymousHash(anonymousHash)
                .ratingClarity(request.ratingClarity())
                .ratingFairness(request.ratingFairness())
                .ratingPressure(request.ratingPressure())
                .ratingWorkload(request.ratingWorkload())
                .ratingSupport(request.ratingSupport())
                .comment(request.comment())
                .semester(request.semester())
                .academicYear(request.academicYear())
                .approved(false)
                .build();
        Review saved = reviewRepository.save(review);
        messagingTemplate.convertAndSend("/topic/admin/reviews", saved.getId());
        return saved;
    }

    @Transactional
    public Report report(ReviewDtos.CreateReportRequest request) {
        Review review = reviewRepository.findById(request.reviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review không tồn tại"));
        Report report = Report.builder().review(review).reason(request.reason()).build();
        return reportRepository.save(report);
    }

    public List<AdminDtos.PendingReviewItem> pendingReviews() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc().stream().map(review -> new AdminDtos.PendingReviewItem(
                review.getId(),
                review.getLecturer().getId(),
                review.getLecturer().getFullName(),
                review.getLecturer().getFaculty().getName(),
                review.getComment(),
                review.getSemester(),
                review.getAcademicYear(),
                review.getRatingClarity(),
                review.getRatingFairness(),
                review.getRatingPressure(),
                review.getRatingWorkload(),
                review.getRatingSupport(),
                review.getCreatedAt(),
                reportRepository.countByReview_Id(review.getId())
        )).toList();
    }

    @Transactional
    public Review moderate(Long reviewId, boolean approved) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review không tồn tại"));
        review.setApproved(approved);
        return reviewRepository.save(review);
    }

    private User requireVerifiedStudent() {
        User user = currentUserService.requireCurrentUser();
        if (!user.isVerified()) {
            throw new ForbiddenException("Tài khoản chưa được xác thực");
        }
        return user;
    }

    private Instant startOfDay() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant endOfDay() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
