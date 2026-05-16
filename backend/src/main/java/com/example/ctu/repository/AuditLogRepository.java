package com.example.ctu.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ctu.entity.AuditLog;
import com.example.ctu.entity.User;

/**
 * Repository for AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs for a specific user.
     */
    Page<AuditLog> findByUser(User user, Pageable pageable);
    
    /**
     * Find audit logs by action type.
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    /**
     * Find audit logs for a specific entity.
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    /**
     * Find audit logs within a time range.
     */
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find audit logs for a specific user and entity.
     */
    Page<AuditLog> findByUserAndEntityTypeAndEntityId(User user, String entityType, Long entityId, Pageable pageable);
    
    /**
     * Count audit logs by action.
     */
    long countByAction(String action);
    
    /**
     * Count audit logs for a specific user.
     */
    long countByUser(User user);
}
