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
 * Notification entity for real-time user notifications.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user", columnList = "user_id"),
    @Index(name = "idx_notifications_read", columnList = "is_read"),
    @Index(name = "idx_notifications_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;
    
    @Column(length = 50)
    private String type; // NEW_FEEDBACK, NEW_REPORT, NEW_USER, SYSTEM, etc.
    
    @Column(name = "related_id")
    private Long relatedId; // ID of the related entity (review, report, etc.)
    
    @Column(name = "is_read")
    private boolean read = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * Mark notification as read.
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}

