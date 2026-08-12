package com.example.ctu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
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

import com.example.ctu.entity.enums.ReportResolution;
import com.example.ctu.entity.enums.ReportStatus;
import com.example.ctu.exception.ConflictException;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_report_review", columnList = "review_id")
})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "reporter_hash", nullable = false, length = 128)
    private String reporterHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant resolvedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @SuppressWarnings("unused")
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = ReportStatus.PENDING;
        }
    }

    public void resolve(ReportResolution resolution, String note, User resolver) {
        if (resolution == null) {
            throw new ConflictException("A report resolution is required");
        }
        if (status != ReportStatus.PENDING) {
            throw new ConflictException("Report has already been resolved");
        }
        if (note == null || note.isBlank()) {
            throw new ConflictException("A resolution note is required");
        }
        status = resolution == ReportResolution.DISMISS
                ? ReportStatus.DISMISSED
                : ReportStatus.ACTIONED;
        resolutionNote = note.trim().length() <= 1000
                ? note.trim()
                : note.trim().substring(0, 1000);
        resolvedBy = resolver;
        resolvedAt = Instant.now();
    }
}

