package com.example.ctu.entity;

import java.time.LocalDateTime;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Audit log entity for tracking administrative actions.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created", columnList = "created_at"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 50)
    private String action; // CREATE, READ, UPDATE, DELETE, APPROVE, REJECT, LOGIN
    
    @Column(length = 100)
    private String entityType; // User, Lecturer, Review, Report, etc.
    
    @Column
    private Long entityId;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String oldValues; // JSON string of previous values
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String newValues; // JSON string of new values
    
    @Column(length = 500)
    private String description; // Human-readable description
    
    @Column(length = 50)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

