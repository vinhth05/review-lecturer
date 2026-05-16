package com.example.ctu.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.entity.Notification;
import com.example.ctu.entity.User;
import com.example.ctu.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing real-time notifications.
 * Supports both database persistence and WebSocket broadcasting.
 */
@Slf4j
@Service
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    public NotificationService(NotificationRepository notificationRepository, 
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Create and send a notification to a user.
     */
    public Notification createNotification(User user, String title, String message, 
                                           String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        
        Notification saved = notificationRepository.save(notification);
        
        // Broadcast via WebSocket
        broadcastNotification(Objects.requireNonNull(user.getId(), "userId"), saved);
        
        return saved;
    }
    
    /**
     * Broadcast notification via WebSocket to the user.
     */
    private void broadcastNotification(Long userId, Notification notification) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(notification, "notification");
        String destination = Objects.requireNonNull(String.valueOf(userId), "userId");
        try {
            messagingTemplate.convertAndSendToUser(
                destination,
                "/queue/notifications",
                notification
            );
        } catch (MessagingException e) {
            log.debug("WebSocket not available for user " + userId, e);
        }
    }
    
    /**
     * Get unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get all notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get notifications of a specific type for a user.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByType(User user, String type, Pageable pageable) {
        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type, pageable);
    }
    
    /**
     * Count unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }
    
    /**
     * Mark a notification as read.
     */
    public void markAsRead(@NonNull Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });
    }
    
    /**
     * Mark all notifications as read for a user.
     */
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user, LocalDateTime.now());
    }
    
    /**
     * Delete a notification.
     */
    public void deleteNotification(@NonNull Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Delete old notifications for a user (cleanup task).
     */
    public void deleteOldNotifications(User user, int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOldNotifications(user, cutoffDate);
    }
    
    /**
     * Notify all admins of a new event.
     */
    public void notifyAllAdmins(String title, String message, String type) {
        // This would typically fetch all admin users and create notifications for each
        log.info("Admin notification: {} - {}", title, message);
    }
}
