package com.example.ctu.dto.admin;

import java.time.Instant;
import java.util.List;

import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class AdminDtos {
    private AdminDtos() {
    }

    public static final class CreateFacultyRequest {
        private final String name;
        private final String code;

        public CreateFacultyRequest(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String name() {
            return name;
        }

        public String code() {
            return code;
        }
    }

    public static final class UpdateFacultyRequest {
        @NotBlank
        private final String name;
        @NotBlank
        private final String code;

        public UpdateFacultyRequest(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String name() {
            return name;
        }

        public String code() {
            return code;
        }
    }

    public static final class FacultyItem {
        private final Long id;
        private final String name;
        private final String code;
        private final Instant createdAt;

        public FacultyItem(Long id, String name, String code, Instant createdAt) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public String name() { return name; }
        public String code() { return code; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class CreateSubjectRequest {
        private final String name;
        private final String code;
        private final Long facultyId;

        public CreateSubjectRequest(String name, String code, Long facultyId) {
            this.name = name;
            this.code = code;
            this.facultyId = facultyId;
        }

        public String name() { return name; }
        public String code() { return code; }
        public Long facultyId() { return facultyId; }
    }

    public static final class UpdateSubjectRequest {
        @NotBlank
        private final String name;
        @NotBlank
        private final String code;
        @NotNull
        private final Long facultyId;

        public UpdateSubjectRequest(String name, String code, Long facultyId) {
            this.name = name;
            this.code = code;
            this.facultyId = facultyId;
        }

        public String name() { return name; }
        public String code() { return code; }
        public Long facultyId() { return facultyId; }
    }

    public static final class SubjectItem {
        private final Long id;
        private final String name;
        private final String code;
        private final Long facultyId;
        private final String facultyName;
        private final Instant createdAt;

        public SubjectItem(Long id, String name, String code, Long facultyId, String facultyName, Instant createdAt) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.facultyId = facultyId;
            this.facultyName = facultyName;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public String name() { return name; }
        public String code() { return code; }
        public Long facultyId() { return facultyId; }
        public String facultyName() { return facultyName; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class CreateLecturerRequest {
        private final String lecturerCode;
        private final String fullName;
        private final Long facultyId;
        private final Long subjectId;

        public CreateLecturerRequest(String lecturerCode, String fullName, Long facultyId, Long subjectId) {
            this.lecturerCode = lecturerCode;
            this.fullName = fullName;
            this.facultyId = facultyId;
            this.subjectId = subjectId;
        }

        public String lecturerCode() { return lecturerCode; }
        public String fullName() { return fullName; }
        public Long facultyId() { return facultyId; }
        public Long subjectId() { return subjectId; }
    }

    public static final class UpdateLecturerRequest {
        @NotBlank
        private final String lecturerCode;
        @NotBlank
        private final String fullName;
        @NotNull
        private final Long facultyId;
        private final Long subjectId;
        @NotNull
        private final LecturerStatus status;

        public UpdateLecturerRequest(String lecturerCode, String fullName, Long facultyId, Long subjectId, LecturerStatus status) {
            this.lecturerCode = lecturerCode;
            this.fullName = fullName;
            this.facultyId = facultyId;
            this.subjectId = subjectId;
            this.status = status;
        }

        public String lecturerCode() { return lecturerCode; }
        public String fullName() { return fullName; }
        public Long facultyId() { return facultyId; }
        public Long subjectId() { return subjectId; }
        public LecturerStatus status() { return status; }
    }

    public static final class LecturerItem {
        private final Long id;
        private final String lecturerCode;
        private final String fullName;
        private final Long facultyId;
        private final String facultyName;
        private final Long subjectId;
        private final String subjectName;
        private final LecturerStatus status;
        private final Instant createdAt;

        public LecturerItem(Long id, String lecturerCode, String fullName, Long facultyId, String facultyName, Long subjectId, String subjectName, LecturerStatus status, Instant createdAt) {
            this.id = id;
            this.lecturerCode = lecturerCode;
            this.fullName = fullName;
            this.facultyId = facultyId;
            this.facultyName = facultyName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.status = status;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public String lecturerCode() { return lecturerCode; }
        public String fullName() { return fullName; }
        public Long facultyId() { return facultyId; }
        public String facultyName() { return facultyName; }
        public Long subjectId() { return subjectId; }
        public String subjectName() { return subjectName; }
        public LecturerStatus status() { return status; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class ImportCtuLecturersRequest {
        private final Integer maxPages;
        private final Long fallbackFacultyId;

        public ImportCtuLecturersRequest(Integer maxPages, Long fallbackFacultyId) {
            this.maxPages = maxPages;
            this.fallbackFacultyId = fallbackFacultyId;
        }

        public Integer maxPages() { return maxPages; }
        public Long fallbackFacultyId() { return fallbackFacultyId; }
    }

    public static final class ImportCtuLecturersResponse {
        private final int pagesScanned;
        private final int fetchedRows;
        private final int imported;
        private final int skipped;
        private final List<String> errors;

        public ImportCtuLecturersResponse(int pagesScanned, int fetchedRows, int imported, int skipped, List<String> errors) {
            this.pagesScanned = pagesScanned;
            this.fetchedRows = fetchedRows;
            this.imported = imported;
            this.skipped = skipped;
            this.errors = errors;
        }

        public int pagesScanned() { return pagesScanned; }
        public int fetchedRows() { return fetchedRows; }
        public int imported() { return imported; }
        public int skipped() { return skipped; }
        public List<String> errors() { return errors; }
    }

    public static final class CreateToxicKeywordRequest {
        @NotBlank
        private final String keyword;

        public CreateToxicKeywordRequest(String keyword) {
            this.keyword = keyword;
        }

        public String keyword() { return keyword; }
    }

    public static final class ToxicKeywordItem {
        private final Long id;
        private final String keyword;
        private final Instant createdAt;

        public ToxicKeywordItem(Long id, String keyword, Instant createdAt) {
            this.id = id;
            this.keyword = keyword;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public String keyword() { return keyword; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class UpdateToxicKeywordRequest {
        @NotBlank
        private final String keyword;

        public UpdateToxicKeywordRequest(String keyword) {
            this.keyword = keyword;
        }

        public String keyword() { return keyword; }
    }

    public static final class ReportItem {
        private final Long id;
        private final Long reviewId;
        private final Long lecturerId;
        private final String lecturerName;
        private final String comment;
        private final String reason;
        private final Instant createdAt;

        public ReportItem(Long id, Long reviewId, Long lecturerId, String lecturerName, String comment, String reason, Instant createdAt) {
            this.id = id;
            this.reviewId = reviewId;
            this.lecturerId = lecturerId;
            this.lecturerName = lecturerName;
            this.comment = comment;
            this.reason = reason;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public Long reviewId() { return reviewId; }
        public Long lecturerId() { return lecturerId; }
        public String lecturerName() { return lecturerName; }
        public String comment() { return comment; }
        public String reason() { return reason; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class ResetUserPasswordRequest {
        @NotBlank
        private final String newPassword;

        public ResetUserPasswordRequest(String newPassword) {
            this.newPassword = newPassword;
        }

        public String newPassword() { return newPassword; }
    }

    public static final class UserItem {
        private final Long id;
        private final String studentCode;
        private final String fullName;
        private final String email;
        private final String facultyName;
        private final Role role;
        private final boolean verified;
        private final boolean locked;
        private final Instant createdAt;

        public UserItem(Long id, String studentCode, String fullName, String email, String facultyName, Role role, boolean verified, boolean locked, Instant createdAt) {
            this.id = id;
            this.studentCode = studentCode;
            this.fullName = fullName;
            this.email = email;
            this.facultyName = facultyName;
            this.role = role;
            this.verified = verified;
            this.locked = locked;
            this.createdAt = createdAt;
        }

        public Long id() { return id; }
        public String studentCode() { return studentCode; }
        public String fullName() { return fullName; }
        public String email() { return email; }
        public String facultyName() { return facultyName; }
        public Role role() { return role; }
        public boolean verified() { return verified; }
        public boolean locked() { return locked; }
        public Instant createdAt() { return createdAt; }
    }

    public static final class UpdateUserRoleRequest {
        @NotNull
        private final Role role;

        public UpdateUserRoleRequest(Role role) {
            this.role = role;
        }

        public Role role() { return role; }
    }

    public static final class PendingReviewItem {
        private final Long id;
        private final Long lecturerId;
        private final String lecturerName;
        private final String facultyName;
        private final String comment;
        private final String semester;
        private final String academicYear;
        private final int ratingClarity;
        private final int ratingFairness;
        private final int ratingPressure;
        private final int ratingWorkload;
        private final int ratingSupport;
        private final Instant createdAt;
        private final long reportCount;

        public PendingReviewItem(Long id, Long lecturerId, String lecturerName, String facultyName, String comment, String semester, String academicYear, int ratingClarity, int ratingFairness, int ratingPressure, int ratingWorkload, int ratingSupport, Instant createdAt, long reportCount) {
            this.id = id;
            this.lecturerId = lecturerId;
            this.lecturerName = lecturerName;
            this.facultyName = facultyName;
            this.comment = comment;
            this.semester = semester;
            this.academicYear = academicYear;
            this.ratingClarity = ratingClarity;
            this.ratingFairness = ratingFairness;
            this.ratingPressure = ratingPressure;
            this.ratingWorkload = ratingWorkload;
            this.ratingSupport = ratingSupport;
            this.createdAt = createdAt;
            this.reportCount = reportCount;
        }

        public Long id() { return id; }
        public Long lecturerId() { return lecturerId; }
        public String lecturerName() { return lecturerName; }
        public String facultyName() { return facultyName; }
        public String comment() { return comment; }
        public String semester() { return semester; }
        public String academicYear() { return academicYear; }
        public int ratingClarity() { return ratingClarity; }
        public int ratingFairness() { return ratingFairness; }
        public int ratingPressure() { return ratingPressure; }
        public int ratingWorkload() { return ratingWorkload; }
        public int ratingSupport() { return ratingSupport; }
        public Instant createdAt() { return createdAt; }
        public long reportCount() { return reportCount; }
    }

    public static final class ReviewItem {
        private final Long id;
        private final Long lecturerId;
        private final String lecturerName;
        private final String facultyName;
        private final String comment;
        private final String semester;
        private final String academicYear;
        private final int ratingClarity;
        private final int ratingFairness;
        private final int ratingPressure;
        private final int ratingWorkload;
        private final int ratingSupport;
        private final boolean approved;
        private final Instant createdAt;
        private final long reportCount;

        public ReviewItem(Long id, Long lecturerId, String lecturerName, String facultyName, String comment, String semester, String academicYear, int ratingClarity, int ratingFairness, int ratingPressure, int ratingWorkload, int ratingSupport, boolean approved, Instant createdAt, long reportCount) {
            this.id = id;
            this.lecturerId = lecturerId;
            this.lecturerName = lecturerName;
            this.facultyName = facultyName;
            this.comment = comment;
            this.semester = semester;
            this.academicYear = academicYear;
            this.ratingClarity = ratingClarity;
            this.ratingFairness = ratingFairness;
            this.ratingPressure = ratingPressure;
            this.ratingWorkload = ratingWorkload;
            this.ratingSupport = ratingSupport;
            this.approved = approved;
            this.createdAt = createdAt;
            this.reportCount = reportCount;
        }

        public Long id() { return id; }
        public Long lecturerId() { return lecturerId; }
        public String lecturerName() { return lecturerName; }
        public String facultyName() { return facultyName; }
        public String comment() { return comment; }
        public String semester() { return semester; }
        public String academicYear() { return academicYear; }
        public int ratingClarity() { return ratingClarity; }
        public int ratingFairness() { return ratingFairness; }
        public int ratingPressure() { return ratingPressure; }
        public int ratingWorkload() { return ratingWorkload; }
        public int ratingSupport() { return ratingSupport; }
        public boolean approved() { return approved; }
        public Instant createdAt() { return createdAt; }
        public long reportCount() { return reportCount; }
    }

    public static final class PageResponse<T> {
        private final List<T> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;

        public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }

        public List<T> content() { return content; }
        public int page() { return page; }
        public int size() { return size; }
        public long totalElements() { return totalElements; }
        public int totalPages() { return totalPages; }
        public boolean first() { return first; }
        public boolean last() { return last; }
    }

    public static final class FacultyStatisticItem {
        private final Long facultyId;
        private final String facultyName;
        private final long lecturerCount;
        private final long reviewCount;
        private final double averageRating;
        private final long pendingReviewCount;

        public FacultyStatisticItem(Long facultyId, String facultyName, long lecturerCount, long reviewCount, double averageRating, long pendingReviewCount) {
            this.facultyId = facultyId;
            this.facultyName = facultyName;
            this.lecturerCount = lecturerCount;
            this.reviewCount = reviewCount;
            this.averageRating = averageRating;
            this.pendingReviewCount = pendingReviewCount;
        }

        public Long facultyId() { return facultyId; }
        public String facultyName() { return facultyName; }
        public long lecturerCount() { return lecturerCount; }
        public long reviewCount() { return reviewCount; }
        public double averageRating() { return averageRating; }
        public long pendingReviewCount() { return pendingReviewCount; }
    }

    public static final class TopLecturerItem {
        private final Long lecturerId;
        private final String lecturerName;
        private final String facultyName;
        private final double averageRating;
        private final long reviewCount;

        public TopLecturerItem(Long lecturerId, String lecturerName, String facultyName, double averageRating, long reviewCount) {
            this.lecturerId = lecturerId;
            this.lecturerName = lecturerName;
            this.facultyName = facultyName;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public Long lecturerId() { return lecturerId; }
        public String lecturerName() { return lecturerName; }
        public String facultyName() { return facultyName; }
        public double averageRating() { return averageRating; }
        public long reviewCount() { return reviewCount; }
    }

    public static final class AdminStatisticResponse {
        private final long totalStudents;
        private final long totalFaculties;
        private final long totalSubjects;
        private final long totalLecturers;
        private final long totalReviews;
        private final long pendingReviews;
        private final List<FacultyStatisticItem> facultyStatistics;
        private final List<TopLecturerItem> topLecturers;
        private final List<TopLecturerItem> topReportedLecturers;

        public AdminStatisticResponse(long totalStudents, long totalFaculties, long totalSubjects, long totalLecturers, long totalReviews, long pendingReviews, List<FacultyStatisticItem> facultyStatistics, List<TopLecturerItem> topLecturers, List<TopLecturerItem> topReportedLecturers) {
            this.totalStudents = totalStudents;
            this.totalFaculties = totalFaculties;
            this.totalSubjects = totalSubjects;
            this.totalLecturers = totalLecturers;
            this.totalReviews = totalReviews;
            this.pendingReviews = pendingReviews;
            this.facultyStatistics = facultyStatistics;
            this.topLecturers = topLecturers;
            this.topReportedLecturers = topReportedLecturers;
        }

        public long totalStudents() { return totalStudents; }
        public long totalFaculties() { return totalFaculties; }
        public long totalSubjects() { return totalSubjects; }
        public long totalLecturers() { return totalLecturers; }
        public long totalReviews() { return totalReviews; }
        public long pendingReviews() { return pendingReviews; }
        public List<FacultyStatisticItem> facultyStatistics() { return facultyStatistics; }
        public List<TopLecturerItem> topLecturers() { return topLecturers; }
        public List<TopLecturerItem> topReportedLecturers() { return topReportedLecturers; }
    }
}
