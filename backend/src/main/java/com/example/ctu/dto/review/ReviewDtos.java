package com.example.ctu.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.ctu.entity.enums.ReviewStatus;
import com.example.ctu.entity.enums.ReportResolution;
import com.example.ctu.entity.enums.ReportStatus;

public final class ReviewDtos {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull Long lecturerId,
            @Min(1) @Max(5) int ratingClarity,
            @Min(1) @Max(5) int ratingFairness,
            @Min(1) @Max(5) int ratingPressure,
            @Min(1) @Max(5) int ratingWorkload,
            @Min(1) @Max(5) int ratingSupport,
            @NotBlank @Size(min = 10, max = 1000) String comment,
            @NotBlank String semester,
            @NotBlank String academicYear
    ) {
    }

    public record CreateReportRequest(
            @NotNull Long reviewId,
            @NotBlank @Size(max = 1000) String reason
    ) {
    }

    public record ResolveReportCommand(
            @NotNull ReportResolution resolution,
            @NotBlank @Size(max = 1000) String note,
            Long expectedVersion
    ) {
    }

    public record ReportResolutionResult(
            Long reportId,
            ReportStatus status,
            String note,
            java.time.Instant resolvedAt,
            long version
    ) {
    }

    public record ModerateReviewRequest(boolean approved) {
    }

    public record ModerateReviewCommand(
            @NotNull ReviewStatus status,
            @Size(max = 1000) String reason,
            Long expectedVersion
    ) {
    }

    public record ModerationResult(
            Long reviewId,
            ReviewStatus status,
            String reason,
            java.time.Instant moderatedAt,
            long version
    ) {
    }

    public record MyReviewItem(
            Long id,
            Long lecturerId,
            String lecturerName,
            int ratingClarity,
            int ratingFairness,
            int ratingPressure,
            int ratingWorkload,
            int ratingSupport,
            double averageRating,
            String comment,
            String semester,
            String academicYear,
            java.time.Instant createdAt,
            ReviewStatus status,
            String moderationReason
    ) {
    }
}
