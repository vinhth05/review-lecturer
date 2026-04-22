package com.example.ctu.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record CreateFacultyRequest(String name, String code) {
    }

    public record CreateSubjectRequest(String name, String code, UUID facultyId) {
    }

    public record CreateLecturerRequest(String lecturerCode, String fullName, UUID facultyId, UUID subjectId) {
    }

    public record ImportCtuLecturersRequest(
            Integer maxPages,
            UUID fallbackFacultyId
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

        public record ToxicKeywordItem(UUID id, String keyword, Instant createdAt) {
        }

    public record PendingReviewItem(
            UUID id,
            UUID lecturerId,
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

    public record FacultyStatisticItem(
            UUID facultyId,
            String facultyName,
            long lecturerCount,
            long reviewCount,
            double averageRating,
            long pendingReviewCount
    ) {
    }

    public record TopLecturerItem(
            UUID lecturerId,
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
