package com.example.ctu.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Review;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.entity.enums.ReviewStatus;
import com.example.ctu.entity.enums.ReportStatus;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ConflictException;
import com.example.ctu.exception.ForbiddenException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReportRepository;
import com.example.ctu.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
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
    private final AuditLogService auditLogService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReportRepository reportRepository,
                         LecturerRepository lecturerRepository,
                         CurrentUserService currentUserService,
                         HashService hashService,
                         ToxicFilterService toxicFilterService,
                         AppPropertiesFacade appPropertiesFacade,
                         SimpMessagingTemplate messagingTemplate,
                         AuditLogService auditLogService) {
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.lecturerRepository = lecturerRepository;
        this.currentUserService = currentUserService;
        this.hashService = hashService;
        this.toxicFilterService = toxicFilterService;
        this.appPropertiesFacade = appPropertiesFacade;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
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
        if (reviewRepository.countByAnonymousHashAndLecturer_Id(anonymousHash, request.lecturerId()) >= 3) {
            throw new BadRequestException("Bạn chỉ được đánh giá giảng viên này tối đa 3 lần");
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
                .status(ReviewStatus.PENDING)
                .build();
        Review saved = reviewRepository.save(review);
        messagingTemplate.convertAndSend("/topic/admin/reviews", saved.getId());
        return saved;
    }

    @Transactional
    public Report report(ReviewDtos.CreateReportRequest request) {
        User reporter = requireVerifiedStudent();
        String reporterHash = hashService.anonymousHash(reporter.getStudentCode());
        Review review = reviewRepository.findById(request.reviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review không tồn tại"));
        if (!review.isApproved() || review.getStatus() == ReviewStatus.REJECTED) {
            throw new ConflictException("Only published reviews can be reported");
        }
        if (reporterHash.equals(review.getAnonymousHash())) {
            throw new ConflictException("You cannot report your own review");
        }
        if (reportRepository.existsByReview_IdAndReporterHashAndStatus(
                review.getId(), reporterHash, ReportStatus.PENDING)) {
            throw new ConflictException("You already have a pending report for this review");
        }
        Report report = Report.builder()
                .review(review)
                .reporterHash(reporterHash)
                .reason(request.reason().trim())
                .status(ReportStatus.PENDING)
                .build();
        return reportRepository.save(report);
    }

    public List<AdminDtos.PendingReviewItem> pendingReviews() {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING).stream().map(review -> new AdminDtos.PendingReviewItem(
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
                reportRepository.countByReview_IdAndStatus(review.getId(), ReportStatus.PENDING),
                review.getStatus(),
                review.getVersion()
        )).toList();
    }

    @Transactional
    public Review moderate(Long reviewId, boolean approved) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review không tồn tại"));
        User moderator = currentUserService.requireCurrentUser();
        review.moderate(
                approved ? ReviewStatus.APPROVED : ReviewStatus.REJECTED,
                approved ? null : "Rejected through legacy moderation endpoint",
                moderator
        );
        Review saved = reviewRepository.save(review);
        auditModeration(saved, moderator);
        return saved;
    }

    @Transactional
    public ReviewDtos.ModerationResult moderate(Long reviewId,
                                                ReviewStatus targetStatus,
                                                String reason,
                                                Long expectedVersion) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (expectedVersion != null && review.getVersion() != expectedVersion) {
            throw new ConflictException("Review was changed by another moderator; reload and try again");
        }

        User moderator = currentUserService.requireCurrentUser();
        review.moderate(targetStatus, reason, moderator);
        Review saved = reviewRepository.save(review);
        auditModeration(saved, moderator);
        return new ReviewDtos.ModerationResult(
                saved.getId(),
                saved.getStatus(),
                saved.getModerationReason(),
                saved.getModeratedAt(),
                saved.getVersion()
        );
    }

    private void auditModeration(Review review, User moderator) {
        auditLogService.logSimple(
                moderator,
                "MODERATE",
                "Review",
                review.getId(),
                "Review transitioned to " + review.getStatus()
        );
        messagingTemplate.convertAndSend("/topic/admin/reviews", review.getId());
    }

    private User requireVerifiedStudent() {
        User user = currentUserService.requireCurrentUser();
        if (!user.isVerified()) {
            throw new ForbiddenException("Tài khoản chưa được xác thực");
        }
        return user;
    }

    public List<ReviewDtos.MyReviewItem> getMyReviews() {
        User user = currentUserService.requireCurrentUser();
        String anonymousHash = hashService.anonymousHash(user.getStudentCode());
        return reviewRepository.findByAnonymousHashOrderByCreatedAtDesc(anonymousHash).stream()
                .map(review -> {
                    double avg = (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0;
                    return new ReviewDtos.MyReviewItem(
                            review.getId(),
                            review.getLecturer().getId(),
                            review.getLecturer().getFullName(),
                            review.getRatingClarity(),
                            review.getRatingFairness(),
                            review.getRatingPressure(),
                            review.getRatingWorkload(),
                            review.getRatingSupport(),
                            avg,
                            review.getComment(),
                            review.getSemester(),
                            review.getAcademicYear(),
                            review.getCreatedAt(),
                            review.getStatus(),
                            review.getModerationReason()
                    );
                }).toList();
    }

    private Instant startOfDay() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant endOfDay() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
