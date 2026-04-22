package com.example.ctu.service;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Subject;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReportRepository;
import com.example.ctu.repository.ReviewRepository;
import com.example.ctu.repository.SubjectRepository;
import com.example.ctu.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final LecturerRepository lecturerRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public AdminService(FacultyRepository facultyRepository,
                        SubjectRepository subjectRepository,
                        LecturerRepository lecturerRepository,
                        ReviewRepository reviewRepository,
                        ReportRepository reportRepository,
                        UserRepository userRepository) {
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.lecturerRepository = lecturerRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Faculty createFaculty(AdminDtos.CreateFacultyRequest request) {
        if (facultyRepository.findByCode(request.code()).isPresent()) {
            throw new BadRequestException("Mã khoa đã tồn tại");
        }
        return facultyRepository.save(Faculty.builder().name(request.name()).code(request.code()).build());
    }

    @Transactional
    public Subject createSubject(AdminDtos.CreateSubjectRequest request) {
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        return subjectRepository.save(Subject.builder().name(request.name()).code(request.code()).faculty(faculty).build());
    }

    @Transactional
    public Lecturer createLecturer(AdminDtos.CreateLecturerRequest request) {
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
        }
        return lecturerRepository.save(Lecturer.builder()
                .lecturerCode(request.lecturerCode())
                .fullName(request.fullName())
                .faculty(faculty)
                .subject(subject)
                .status(LecturerStatus.ACTIVE)
                .build());
    }

    @Transactional
    public Lecturer hideLecturer(UUID lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        lecturer.setStatus(LecturerStatus.HIDDEN);
        return lecturerRepository.save(lecturer);
    }

    public AdminDtos.AdminStatisticResponse statistics() {
        List<Lecturer> lecturers = lecturerRepository.findAll();
        List<com.example.ctu.entity.Review> approvedReviews = reviewRepository.findAll().stream()
                .filter(com.example.ctu.entity.Review::isApproved)
                .toList();
        List<com.example.ctu.entity.Review> pendingReviews = reviewRepository.findAll().stream()
                .filter(review -> !review.isApproved())
                .toList();
        List<Report> reports = reportRepository.findAll();

        List<AdminDtos.FacultyStatisticItem> facultyStatistics = facultyRepository.findAll().stream()
                .map(faculty -> {
                    List<Lecturer> facultyLecturers = lecturers.stream().filter(lecturer -> lecturer.getFaculty().getId().equals(faculty.getId())).toList();
                    List<com.example.ctu.entity.Review> facultyReviews = approvedReviews.stream()
                            .filter(review -> review.getLecturer().getFaculty().getId().equals(faculty.getId()))
                            .toList();
                    long pending = pendingReviews.stream()
                            .filter(review -> review.getLecturer().getFaculty().getId().equals(faculty.getId()))
                            .count();
                    return new AdminDtos.FacultyStatisticItem(
                            faculty.getId(),
                            faculty.getName(),
                            facultyLecturers.size(),
                            facultyReviews.size(),
                            averageRating(facultyReviews),
                            pending
                    );
                })
                .sorted(Comparator.comparing(AdminDtos.FacultyStatisticItem::facultyName))
                .toList();

        List<AdminDtos.TopLecturerItem> topLecturers = lecturers.stream()
                .map(lecturer -> new AdminDtos.TopLecturerItem(
                        lecturer.getId(),
                        lecturer.getFullName(),
                        lecturer.getFaculty().getName(),
                        averageRating(approvedReviews.stream().filter(review -> review.getLecturer().getId().equals(lecturer.getId())).toList()),
                        reviewRepository.countByLecturer_IdAndApproved(lecturer.getId(), true)
                ))
                .sorted(Comparator.comparing(AdminDtos.TopLecturerItem::averageRating).reversed())
                .limit(10)
                .toList();

        Map<UUID, Long> reportCountByLecturer = reports.stream()
                .collect(Collectors.groupingBy(report -> report.getReview().getLecturer().getId(), Collectors.counting()));

        List<AdminDtos.TopLecturerItem> topReportedLecturers = lecturers.stream()
                .map(lecturer -> new AdminDtos.TopLecturerItem(
                        lecturer.getId(),
                        lecturer.getFullName(),
                        lecturer.getFaculty().getName(),
                        averageRating(approvedReviews.stream().filter(review -> review.getLecturer().getId().equals(lecturer.getId())).toList()),
                        reportCountByLecturer.getOrDefault(lecturer.getId(), 0L)
                ))
                .sorted(Comparator.comparing(AdminDtos.TopLecturerItem::reviewCount).reversed())
                .limit(10)
                .toList();

        return new AdminDtos.AdminStatisticResponse(
                userRepository.count(),
                facultyRepository.count(),
                subjectRepository.count(),
                lecturerRepository.count(),
                reviewRepository.count(),
                pendingReviews.size(),
                facultyStatistics,
                topLecturers,
                topReportedLecturers
        );
    }

    private double averageRating(List<com.example.ctu.entity.Review> reviews) {
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToDouble(review ->
                (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0
        ).average().orElse(0);
    }
}
