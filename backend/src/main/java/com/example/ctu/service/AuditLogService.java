package com.example.ctu.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.entity.AuditLog;
import com.example.ctu.entity.User;
import com.example.ctu.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing audit logs.
 */
@Slf4j
@Service
@Transactional
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Log an administrative action.
     */
    public AuditLog log(User user, String action, String entityType, Long entityId, 
                        Object oldValues, Object newValues, String description, 
                        String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            
            auditLog.setDescription(description);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            
            return auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
            throw new RuntimeException("Failed to create audit log", e);
        }
    }
    
    /**
     * Log a simple action without complex values.
     */
    public AuditLog logSimple(User user, String action, String entityType, Long entityId, String description) {
        return log(user, action, entityType, entityId, null, null, description, null, null);
    }
    
    /**
     * Get audit logs for a specific user.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(User user, Pageable pageable) {
        return auditLogRepository.findByUser(user, pageable);
    }
    
    /**
     * Get audit logs by action type.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }
    
    /**
     * Get audit logs for a specific entity.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
    
    /**
     * Get audit logs within a time range.
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    /**
     * Delete old audit logs (older than specified days).
     */
    public long deleteOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        java.util.List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBetween(
            LocalDateTime.now().minusYears(10), 
            cutoffDate
        );
        oldLogs.forEach(auditLogRepository::delete);
        return oldLogs.size();
    }
    
    /**
     * Get audit log statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", auditLogRepository.count());
        return stats;
    }
}
