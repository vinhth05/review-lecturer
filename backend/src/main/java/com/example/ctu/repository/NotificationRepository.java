package com.example.ctu.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ctu.entity.Notification;
import com.example.ctu.entity.User;

/**
 * Repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find unread notifications for a user.
     */
    Page<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find all notifications for a user.
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find notifications by type for a user.
     */
    Page<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type, Pageable pageable);
    
    /**
     * Count unread notifications for a user.
     */
    long countByUserAndReadFalse(User user);
    
    /**
     * Find notifications within a time range for a user.
     */
    List<Notification> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * Mark notifications as read for a user.
     */
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.user = :user AND n.read = false")
    void markAllAsRead(@Param("user") User user, @Param("now") LocalDateTime now);
    
    /**
     * Delete old notifications (older than specified date).
     */
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count notifications by type for a user.
     */
    long countByUserAndType(User user, String type);
}
