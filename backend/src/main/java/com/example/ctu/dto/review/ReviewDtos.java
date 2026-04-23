package com.example.ctu.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull Long lecturerId,
            @Min(1) @Max(5) int ratingClarity,
            @Min(1) @Max(5) int ratingFairness,
            @Min(1) @Max(5) int ratingPressure,
            @Min(1) @Max(5) int ratingWorkload,
            @Min(1) @Max(5) int ratingSupport,
            @NotBlank String comment,
            @NotBlank String semester,
            @NotBlank String academicYear
    ) {
    }

    public record CreateReportRequest(
            @NotNull Long reviewId,
            @NotBlank String reason
    ) {
    }

    public record ModerateReviewRequest(boolean approved) {
    }
}
