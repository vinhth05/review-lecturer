package com.example.ctu.dto.lecturer;

import com.example.ctu.entity.enums.LecturerStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LecturerDtos {
    private LecturerDtos() {
    }

    public record LecturerSummaryResponse(
            UUID id,
            String lecturerCode,
            String fullName,
            String facultyName,
            String subjectName,
            LecturerStatus status,
            double averageRating,
            long reviewCount
    ) {
    }

    public record RatingPointResponse(String label, double value) {
    }

    public record RatingDistributionItem(int score, long count) {
    }

    public record SemesterComparisonItem(String semester, String academicYear, double averageRating, long reviewCount) {
    }

    public record LecturerDetailResponse(
            UUID id,
            String lecturerCode,
            String fullName,
            String facultyName,
            String subjectName,
            LecturerStatus status,
            double averageClarity,
            double averageFairness,
            double averagePressure,
            double averageWorkload,
            double averageSupport,
            long reviewCount,
            List<RatingDistributionItem> distribution,
            List<SemesterComparisonItem> semesterComparisons,
            List<ReviewItem> latestReviews
    ) {
    }

    public record ReviewItem(
            UUID id,
            int ratingClarity,
            int ratingFairness,
            int ratingPressure,
            int ratingWorkload,
            int ratingSupport,
            String comment,
            String semester,
            String academicYear,
            Instant createdAt
    ) {
    }
}
