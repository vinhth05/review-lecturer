package com.example.ctu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import com.example.ctu.entity.enums.ReviewStatus;
import com.example.ctu.exception.ConflictException;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_lecturer_approved", columnList = "lecturer_id, is_approved"),
    @Index(name = "idx_review_anonymous", columnList = "anonymous_hash"),
    @Index(name = "idx_review_created", columnList = "created_at")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @Column(name = "anonymous_hash", nullable = false, length = 128)
    private String anonymousHash;

    @Column(name = "rating_clarity", nullable = false)
    private Integer ratingClarity;

    @Column(name = "rating_fairness", nullable = false)
    private Integer ratingFairness;

    @Column(name = "rating_pressure", nullable = false)
    private Integer ratingPressure;

    @Column(name = "rating_workload", nullable = false)
    private Integer ratingWorkload;

    @Column(name = "rating_support", nullable = false)
    private Integer ratingSupport;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(nullable = false, length = 50)
    private String semester;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "is_approved", nullable = false)
    private boolean approved;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    private ReviewStatus status;

    @Column(name = "moderation_reason", length = 1000)
    private String moderationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private User moderatedBy;

    @Column(name = "moderated_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant moderatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = approved ? ReviewStatus.APPROVED : ReviewStatus.PENDING;
        }
        approved = status == ReviewStatus.APPROVED;
    }

    /**
     * Apply a business transition instead of exposing moderation as a CRUD
     * boolean toggle. Repeating the same command is intentionally idempotent.
     */
    public void moderate(ReviewStatus targetStatus, String reason, User moderator) {
        if (targetStatus == null || targetStatus == ReviewStatus.PENDING) {
            throw new ConflictException("Moderation target must be APPROVED or REJECTED");
        }

        ReviewStatus currentStatus = status == null
                ? (approved ? ReviewStatus.APPROVED : ReviewStatus.PENDING)
                : status;
        if (currentStatus == targetStatus) {
            return;
        }
        if (currentStatus == ReviewStatus.REJECTED) {
            throw new ConflictException("A rejected review cannot be moderated again");
        }
        if (targetStatus == ReviewStatus.REJECTED && (reason == null || reason.isBlank())) {
            throw new ConflictException("A rejection reason is required");
        }

        status = targetStatus;
        approved = targetStatus == ReviewStatus.APPROVED;
        moderationReason = normalizeReason(reason);
        moderatedBy = moderator;
        moderatedAt = Instant.now();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String normalized = reason.trim();
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000);
    }
}

