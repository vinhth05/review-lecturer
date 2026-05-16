package com.example.ctu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ctu.entity.Settings;

/**
 * Repository for Settings entities.
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    
    /**
     * Find a setting by key.
     */
    Optional<Settings> findByKey(String key);
    
    /**
     * Check if a setting exists by key.
     */
    boolean existsByKey(String key);
    
    /**
     * Find all settings by value type.
     */
    List<Settings> findByValueType(String valueType);
    
    /**
     * Delete setting by key.
     */
    void deleteByKey(String key);
}
