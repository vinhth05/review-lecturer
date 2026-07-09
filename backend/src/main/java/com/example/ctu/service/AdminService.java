package com.example.ctu.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Subject;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ForbiddenException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReportRepository;
import com.example.ctu.repository.ReviewRepository;
import com.example.ctu.repository.SubjectRepository;
import com.example.ctu.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class AdminService {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final LecturerRepository lecturerRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    public AdminService(FacultyRepository facultyRepository,
            SubjectRepository subjectRepository,
            LecturerRepository lecturerRepository,
            ReviewRepository reviewRepository,
            ReportRepository reportRepository,
            UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            CurrentUserService currentUserService) {
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.lecturerRepository = lecturerRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Faculty createFaculty(AdminDtos.CreateFacultyRequest request) {
        if (facultyRepository.findByCode(request.code()).isPresent()) {
            throw new BadRequestException("Mã khoa đã tồn tại");
        }
        Faculty faculty = facultyRepository.save(Faculty.builder().name(request.name()).code(request.code()).build());

        // Audit logging
        try {
            User currentUser = currentUserService.requireCurrentUser();
            if (currentUser != null) {
                auditLogService.logSimple(currentUser, "CREATE", "Faculty", faculty.getId(),
                        "Tạo mới khoa: " + faculty.getName());
            }
        } catch (Exception e) {
            // Silently fail audit logging to not disrupt main operation
        }

        return faculty;
    }

    @Transactional
    public Subject createSubject(AdminDtos.CreateSubjectRequest request) {
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        if (subjectRepository.existsByCode(request.code())) {
            throw new BadRequestException("Mã môn học đã tồn tại");
        }
        return subjectRepository.save(Subject.builder().name(request.name()).code(request.code()).faculty(faculty).build());
    }

    @Transactional
    public Lecturer createLecturer(AdminDtos.CreateLecturerRequest request) {
        if (lecturerRepository.existsByLecturerCode(request.lecturerCode())) {
            throw new BadRequestException("Mã giảng viên đã tồn tại");
        }
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
            ensureSubjectBelongsToFaculty(subject, faculty);
        }
        return lecturerRepository.save(Lecturer.builder()
                .lecturerCode(request.lecturerCode())
                .fullName(request.fullName())
                .faculty(faculty)
                .subject(subject)
                .status(LecturerStatus.ACTIVE)
                .build());
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.FacultyItem> listFaculties(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name"));
        Page<Faculty> facultyPage = facultyRepository.findAll(pageable);
        List<AdminDtos.FacultyItem> content = facultyPage.getContent().stream()
                .map(faculty -> new AdminDtos.FacultyItem(faculty.getId(), faculty.getName(), faculty.getCode(), null))
                .toList();
        return new AdminDtos.PageResponse<>(content, facultyPage.getNumber(), facultyPage.getSize(), facultyPage.getTotalElements(), facultyPage.getTotalPages(), facultyPage.isFirst(), facultyPage.isLast());
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.SubjectItem> listSubjects(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "faculty.name").and(Sort.by(Sort.Direction.ASC, "name")));
        Page<Subject> subjectPage = subjectRepository.findAll(pageable);
        List<AdminDtos.SubjectItem> content = subjectPage.getContent().stream()
                .map(subject -> new AdminDtos.SubjectItem(
                subject.getId(),
                subject.getName(),
                subject.getCode(),
                subject.getFaculty().getId(),
                subject.getFaculty().getName(),
                subject.getCreatedAt()))
                .toList();
        return new AdminDtos.PageResponse<>(content, subjectPage.getNumber(), subjectPage.getSize(), subjectPage.getTotalElements(), subjectPage.getTotalPages(), subjectPage.isFirst(), subjectPage.isLast());
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.LecturerItem> listLecturers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Lecturer> lecturerPage = lecturerRepository.findAll(pageable);
        List<AdminDtos.LecturerItem> content = lecturerPage.getContent().stream()
                .map(lecturer -> new AdminDtos.LecturerItem(
                lecturer.getId(),
                lecturer.getLecturerCode(),
                lecturer.getFullName(),
                lecturer.getFaculty().getId(),
                lecturer.getFaculty().getName(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getId(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getName(),
                lecturer.getStatus(),
                lecturer.getCreatedAt()))
                .toList();
        return new AdminDtos.PageResponse<>(content, lecturerPage.getNumber(), lecturerPage.getSize(), lecturerPage.getTotalElements(), lecturerPage.getTotalPages(), lecturerPage.isFirst(), lecturerPage.isLast());
    }

    @Transactional
    public Faculty updateFaculty(Long facultyId, AdminDtos.UpdateFacultyRequest request) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        if (!faculty.getCode().equalsIgnoreCase(request.code()) && facultyRepository.findByCode(request.code()).isPresent()) {
            throw new BadRequestException("Mã khoa đã tồn tại");
        }
        faculty.setName(request.name());
        faculty.setCode(request.code());
        return facultyRepository.save(faculty);
    }

    @Transactional
    public void deleteFaculty(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        if (subjectRepository.countByFaculty_Id(facultyId) > 0) {
            throw new BadRequestException("Không thể xóa khoa đang có môn học");
        }
        if (lecturerRepository.countByFaculty_Id(facultyId) > 0) {
            throw new BadRequestException("Không thể xóa khoa đang có giảng viên");
        }
        if (userRepository.countByFaculty_Id(facultyId) > 0) {
            throw new BadRequestException("Không thể xóa khoa đang có người dùng");
        }
        facultyRepository.delete(faculty);

        // Audit logging
        try {
            User currentUser = currentUserService.requireCurrentUser();
            if (currentUser != null) {
                auditLogService.logSimple(currentUser, "DELETE", "Faculty", faculty.getId(),
                        "Xóa khoa: " + faculty.getName());
            }
        } catch (Exception e) {
            // Silently fail audit logging to not disrupt main operation
        }
    }

    @Transactional
    public Subject updateSubject(Long subjectId, AdminDtos.UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        if (!subject.getCode().equalsIgnoreCase(request.code()) && subjectRepository.existsByCode(request.code())) {
            throw new BadRequestException("Mã môn học đã tồn tại");
        }
        subject.setName(request.name());
        subject.setCode(request.code());
        subject.setFaculty(faculty);
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
        if (lecturerRepository.countBySubject_Id(subjectId) > 0) {
            throw new BadRequestException("Không thể xóa môn học đang gán cho giảng viên");
        }
        subjectRepository.delete(subject);
    }

    @Transactional
    public Lecturer updateLecturer(Long lecturerId, AdminDtos.UpdateLecturerRequest request) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        if (!lecturer.getLecturerCode().equalsIgnoreCase(request.lecturerCode()) && lecturerRepository.existsByLecturerCode(request.lecturerCode())) {
            throw new BadRequestException("Mã giảng viên đã tồn tại");
        }
        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
            ensureSubjectBelongsToFaculty(subject, faculty);
        }
        lecturer.setLecturerCode(request.lecturerCode());
        lecturer.setFullName(request.fullName());
        lecturer.setFaculty(faculty);
        lecturer.setSubject(subject);
        lecturer.setStatus(request.status());
        return lecturerRepository.save(lecturer);
    }

    @Transactional
    public void deleteLecturer(Long lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        if (reviewRepository.countByLecturer_Id(lecturerId) > 0) {
            throw new BadRequestException("Không thể xóa giảng viên đã có review");
        }
        lecturerRepository.delete(lecturer);
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.ReportItem> listReports(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reportPage = reportRepository.findAll(pageable);
        List<AdminDtos.ReportItem> content = reportPage.getContent().stream()
                .map(report -> new AdminDtos.ReportItem(
                report.getId(),
                report.getReview().getId(),
                report.getReview().getLecturer().getId(),
                report.getReview().getLecturer().getFullName(),
                report.getReview().getComment(),
                report.getReason(),
                report.getCreatedAt()))
                .toList();
        return new AdminDtos.PageResponse<>(content, reportPage.getNumber(), reportPage.getSize(), reportPage.getTotalElements(), reportPage.getTotalPages(), reportPage.isFirst(), reportPage.isLast());
    }

    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report không tồn tại"));
        reportRepository.delete(report);
    }

    @Transactional
    public String resetUserPassword(Long userId, AdminDtos.ResetUserPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        String newPassword = request.newPassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Audit logging
        try {
            User currentUser = currentUserService.requireCurrentUser();
            if (currentUser != null) {
                auditLogService.logSimple(currentUser, "UPDATE", "User", userId,
                        "Đặt lại mật khẩu cho người dùng: " + user.getEmail());
            }
        } catch (Exception e) {
            // Silently fail audit logging
        }

        return newPassword;
    }

    @Transactional
    public void bulkDeleteReports(List<Long> reportIds) {
        List<Report> reports = reportRepository.findAllById(reportIds);
        reportRepository.deleteAll(reports);
    }

    @Transactional
    public Lecturer hideLecturer(Long lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        lecturer.setStatus(LecturerStatus.HIDDEN);
        return lecturerRepository.save(lecturer);
    }

    @Transactional(readOnly = true)
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

        Map<Long, Long> reportCountByLecturer = reports.stream()
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

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.ReviewItem> listReviews(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<com.example.ctu.entity.Review> reviewPage = reviewRepository.findAll(pageable);

        List<AdminDtos.ReviewItem> content = reviewPage.getContent().stream()
                .map(review -> new AdminDtos.ReviewItem(
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
                review.isApproved(),
                review.getCreatedAt(),
                reportRepository.countByReview_Id(review.getId())
        ))
                .toList();

        return new AdminDtos.PageResponse<>(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isFirst(),
                reviewPage.isLast()
        );
    }

    private double averageRating(List<com.example.ctu.entity.Review> reviews) {
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToDouble(review
                -> (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0
        ).average().orElse(0);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review không tồn tại");
        }
        reportRepository.findAll().stream()
                .filter(report -> report.getReview().getId().equals(reviewId))
                .forEach(report -> reportRepository.delete(report));
        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    public void bulkDeleteReviews(List<Long> reviewIds) {
        reviewIds.forEach(this::deleteReview);
    }

    @Transactional
    public Lecturer unhideLecturer(Long lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        lecturer.setStatus(LecturerStatus.ACTIVE);
        return lecturerRepository.save(lecturer);
    }

    @Transactional
    public void bulkApproveReviews(List<Long> reviewIds) {
        List<com.example.ctu.entity.Review> reviews = reviewRepository.findAllById(reviewIds);
        reviews.forEach(r -> r.setApproved(true));
        reviewRepository.saveAll(reviews);
    }

    @Transactional
    public AdminDtos.UserItem setUserVerified(Long userId, boolean verified) {
        User user = findAdminUser(userId);
        user.setVerified(verified);
        return toUserItem(userRepository.save(user));
    }

    @Transactional
    public AdminDtos.UserItem lockUser(Long userId, User actor) {
        if (actor.getId().equals(userId)) {
            throw new BadRequestException("Không thể khóa chính bạn");
        }
        User target = findAdminUser(userId);
        ensureSuperAdminSafety(target, true);
        target.setLocked(true);
        User saved = userRepository.save(target);

        // Audit logging
        try {
            auditLogService.logSimple(actor, "UPDATE", "User", userId,
                    "Khóa người dùng: " + target.getEmail());
        } catch (Exception e) {
            // Silently fail audit logging
        }

        return toUserItem(saved);
    }

    @Transactional
    public AdminDtos.UserItem unlockUser(Long userId, User actor) {
        if (actor.getId().equals(userId)) {
            throw new BadRequestException("Không thể mở khóa chính bạn");
        }
        User target = findAdminUser(userId);
        target.setLocked(false);
        User saved = userRepository.save(target);

        // Audit logging
        try {
            auditLogService.logSimple(actor, "UPDATE", "User", userId,
                    "Mở khóa người dùng: " + target.getEmail());
        } catch (Exception e) {
            // Silently fail audit logging
        }

        return toUserItem(saved);
    }

    @Transactional
    public void deleteUser(Long userId, User actor) {
        User target = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        if (target.getRole() == Role.SUPER_ADMIN && userRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new BadRequestException("Không thể xóa SUPER_ADMIN cuối cùng");
        }
        if (actor.getId().equals(userId)) {
            throw new BadRequestException("Không thể xóa chính bạn");
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public String exportUsersCsv() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,studentCode,fullName,email,faculty,role,verified,createdAt\n");
        for (User u : users) {
            sb.append(u.getId()).append(',')
                    .append(escapeCsv(u.getStudentCode())).append(',')
                    .append(escapeCsv(u.getFullName())).append(',')
                    .append(escapeCsv(u.getEmail())).append(',')
                    .append(escapeCsv(u.getFaculty().getName())).append(',')
                    .append(u.getRole()).append(',')
                    .append(u.isVerified()).append(',')
                    .append(u.getCreatedAt()).append('\n');
        }
        return sb.toString();
    }

    private User findAdminUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }

    private void ensureSuperAdminSafety(User target, boolean lockAction) {
        if (target.getRole() == Role.SUPER_ADMIN && userRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new BadRequestException(lockAction ? "Không thể khóa SUPER_ADMIN cuối cùng" : "Không thể mở khóa SUPER_ADMIN cuối cùng");
        }
    }

    private void ensureSubjectBelongsToFaculty(Subject subject, Faculty faculty) {
        if (subject.getFaculty() == null || !subject.getFaculty().getId().equals(faculty.getId())) {
            throw new BadRequestException("Môn học không thuộc khoa đã chọn");
        }
    }

    private AdminDtos.UserItem toUserItem(User user) {
        return new AdminDtos.UserItem(
                user.getId(),
                user.getStudentCode(),
                user.getFullName(),
                user.getEmail(),
                user.getFaculty().getName(),
                user.getRole(),
                user.isVerified(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public String exportReviewsCsv(Boolean approved) {
        List<com.example.ctu.entity.Review> reviews = reviewRepository.findAll();
        if (approved != null) {
            reviews = reviews.stream().filter(r -> r.isApproved() == approved).toList();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("id,lecturerId,lecturerName,faculty,comment,semester,academicYear,approved,createdAt\n");
        for (com.example.ctu.entity.Review r : reviews) {
            sb.append(r.getId()).append(',')
                    .append(r.getLecturer().getId()).append(',')
                    .append(escapeCsv(r.getLecturer().getFullName())).append(',')
                    .append(escapeCsv(r.getLecturer().getFaculty().getName())).append(',')
                    .append(escapeCsv(r.getComment())).append(',')
                    .append(escapeCsv(r.getSemester())).append(',')
                    .append(escapeCsv(r.getAcademicYear())).append(',')
                    .append(r.isApproved()).append(',')
                    .append(r.getCreatedAt()).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"")) {
            return '"' + v + '"';
        }
        return v;
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.UserItem> listUsers(int page, int size, String keyword, Role role, Boolean verified) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<User> specification = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("studentCode")), pattern)
            ));
        }
        if (role != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (verified != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("verified"), verified));
        }

        Page<User> userPage = userRepository.findAll(specification, pageable);
        List<AdminDtos.UserItem> content = userPage.getContent().stream()
                .map(user -> new AdminDtos.UserItem(
                user.getId(),
                user.getStudentCode(),
                user.getFullName(),
                user.getEmail(),
                user.getFaculty().getName(),
                user.getRole(),
                user.isVerified(),
                user.isLocked(),
                user.getCreatedAt()
        ))
                .toList();

        return new AdminDtos.PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    @Transactional
    public AdminDtos.UserItem updateUserRole(Long userId, AdminDtos.UpdateUserRoleRequest request, User actor) {
        if (actor.getRole() != Role.SUPER_ADMIN) {
            throw new ForbiddenException("Chỉ SUPER_ADMIN mới được đổi vai trò tài khoản");
        }
        if (actor.getId().equals(userId)) {
            throw new BadRequestException("Không thể tự thay đổi vai trò của chính bạn");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Role newRole = request.role();
        if (target.getRole() == Role.SUPER_ADMIN && newRole != Role.SUPER_ADMIN
                && userRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new BadRequestException("Không thể hạ quyền SUPER_ADMIN cuối cùng");
        }

        target.setRole(newRole);
        return toUserItem(userRepository.save(target));
    }
}
