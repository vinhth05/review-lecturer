package com.example.ctu.dto.admin;

import java.time.Instant;
import java.util.List;

import com.example.ctu.entity.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record CreateFacultyRequest(String name, String code) {
    }

    public record UpdateFacultyRequest(@NotBlank String name, @NotBlank String code) {
    }

    public record FacultyItem(Long id, String name, String code, Instant createdAt) {
    }

    public record CreateSubjectRequest(String name, String code, Long facultyId) {
    }

    public record UpdateSubjectRequest(@NotBlank String name, @NotBlank String code, @NotNull Long facultyId) {
    }

    public record SubjectItem(Long id, String name, String code, Long facultyId, String facultyName, Instant createdAt) {
    }

    public record CreateLecturerRequest(String lecturerCode, String fullName, Long facultyId, Long subjectId) {
    }

    public record UpdateLecturerRequest(
            @NotBlank String lecturerCode,
            @NotBlank String fullName,
            @NotNull Long facultyId,
            Long subjectId,
            @NotNull com.example.ctu.entity.enums.LecturerStatus status
    ) {
    }

    public record LecturerItem(
            Long id,
            String lecturerCode,
            String fullName,
            Long facultyId,
            String facultyName,
            Long subjectId,
            String subjectName,
            com.example.ctu.entity.enums.LecturerStatus status,
            Instant createdAt
    ) {
    }

    public record ImportCtuLecturersRequest(
            Integer maxPages,
            Long fallbackFacultyId
    ) {
    }

    public record ImportCtuLecturersResponse(
            int pagesScanned,
            int fetchedRows,
            int imported,
            int skipped,
            List<String> errors
    ) {
    }

        public record CreateToxicKeywordRequest(@NotBlank String keyword) {
        }

        public record ToxicKeywordItem(Long id, String keyword, Instant createdAt) {
        }

            public record UserItem(
                    Long id,
                    String studentCode,
                    String fullName,
                    String email,
                    String facultyName,
                    Role role,
                    boolean verified,
                    Instant createdAt
            ) {
            }

            public record UpdateUserRoleRequest(@NotNull Role role) {
            }

    public record PendingReviewItem(
                        Long id,
                        Long lecturerId,
            String lecturerName,
            String facultyName,
            String comment,
            String semester,
            String academicYear,
            int ratingClarity,
            int ratingFairness,
            int ratingPressure,
            int ratingWorkload,
            int ratingSupport,
            Instant createdAt,
            long reportCount
    ) {
    }

    public record ReviewItem(
            Long id,
            Long lecturerId,
            String lecturerName,
            String facultyName,
            String comment,
            String semester,
            String academicYear,
            int ratingClarity,
            int ratingFairness,
            int ratingPressure,
            int ratingWorkload,
            int ratingSupport,
            boolean approved,
            Instant createdAt,
            long reportCount
    ) {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }

    public record FacultyStatisticItem(
            Long facultyId,
            String facultyName,
            long lecturerCount,
            long reviewCount,
            double averageRating,
            long pendingReviewCount
    ) {
    }

    public record TopLecturerItem(
            Long lecturerId,
            String lecturerName,
            String facultyName,
            double averageRating,
            long reviewCount
    ) {
    }

    public record AdminStatisticResponse(
            long totalStudents,
            long totalFaculties,
            long totalSubjects,
            long totalLecturers,
            long totalReviews,
            long pendingReviews,
            List<FacultyStatisticItem> facultyStatistics,
            List<TopLecturerItem> topLecturers,
            List<TopLecturerItem> topReportedLecturers
    ) {
    }
}
