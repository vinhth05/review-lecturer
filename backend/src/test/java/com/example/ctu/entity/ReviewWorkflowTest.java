package com.example.ctu.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.ctu.entity.enums.ReviewStatus;
import com.example.ctu.exception.ConflictException;

class ReviewWorkflowTest {

    private final User moderator = User.builder().id(99L).build();

    @Test
    void pendingReview_canBeApprovedIdempotently() {
        Review review = Review.builder().id(1L).status(ReviewStatus.PENDING).build();

        review.moderate(ReviewStatus.APPROVED, null, moderator);
        review.moderate(ReviewStatus.APPROVED, null, moderator);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(review.isApproved()).isTrue();
        assertThat(review.getModeratedBy()).isSameAs(moderator);
        assertThat(review.getModeratedAt()).isNotNull();
    }

    @Test
    void rejection_requiresReason() {
        Review review = Review.builder().status(ReviewStatus.PENDING).build();

        assertThatThrownBy(() -> review.moderate(ReviewStatus.REJECTED, " ", moderator))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void rejectedReview_cannotBeReopenedByAccident() {
        Review review = Review.builder().status(ReviewStatus.PENDING).build();
        review.moderate(ReviewStatus.REJECTED, "Violates moderation policy", moderator);

        assertThatThrownBy(() -> review.moderate(ReviewStatus.APPROVED, null, moderator))
                .isInstanceOf(ConflictException.class);
    }
}
