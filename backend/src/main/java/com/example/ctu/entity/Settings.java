package com.example.ctu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Settings entity for storing application configuration.
 */
@Entity
@Table(name = "settings", indexes = {
    @Index(name = "idx_settings_key", columnList = "key_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_name", nullable = false, unique = true, length = 100)
    private String key;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String value;
    
    @Column(length = 50)
    private String valueType; // STRING, NUMBER, BOOLEAN, JSON
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "is_sensitive")
    private boolean sensitive; // For passwords, api keys, etc.
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
