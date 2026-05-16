package com.example.ctu.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.entity.Settings;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.SettingsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing application settings with caching support.
 */
@Slf4j
@Service
@Transactional
public class SettingsService {
    
    private final SettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;
    
    public SettingsService(SettingsRepository settingsRepository, ObjectMapper objectMapper) {
        this.settingsRepository = settingsRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Get setting value by key (cached).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public String getSetting(String key, String defaultValue) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .orElse(defaultValue);
    }
    
    /**
     * Get setting as string.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public String getSettingAsString(String key) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
    }
    
    /**
     * Get setting as boolean.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public Boolean getSettingAsBoolean(String key) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }
    
    /**
     * Get setting as integer.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public Integer getSettingAsInteger(String key) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .map(SettingsService::parseInteger)
                .orElse(null);
    }
    
    /**
     * Get setting as JSON object.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSettingAsJson(String key) {
        Optional<Settings> setting = settingsRepository.findByKey(key);
        if (setting.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(setting.get().getValue(), 
                new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON setting", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Set or update a setting.
     */
    @CacheEvict(value = "settings", key = "#key")
    public Settings setSetting(String key, String value, String valueType, String description) {
        Optional<Settings> existing = settingsRepository.findByKey(key);
        Settings setting;
        
        if (existing.isPresent()) {
            setting = existing.get();
            setting.setValue(value);
            setting.setValueType(valueType);
            if (description != null) {
                setting.setDescription(description);
            }
        } else {
            setting = new Settings();
            setting.setKey(key);
            setting.setValue(value);
            setting.setValueType(valueType);
            setting.setDescription(description);
        }
        
        return settingsRepository.save(setting);
    }
    
    /**
     * Set setting as boolean.
     */
    @CacheEvict(value = "settings", key = "#key")
    public Settings setSettingAsBoolean(String key, Boolean value, String description) {
        return setSetting(key, value.toString(), "BOOLEAN", description);
    }
    
    /**
     * Set setting as integer.
     */
    @CacheEvict(value = "settings", key = "#key")
    public Settings setSettingAsInteger(String key, Integer value, String description) {
        return setSetting(key, value.toString(), "NUMBER", description);
    }
    
    /**
     * Set setting as JSON.
     */
    @CacheEvict(value = "settings", key = "#key")
    public Settings setSettingAsJson(String key, Map<String, Object> value, String description) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            return setSetting(key, jsonValue, "JSON", description);
        } catch (JsonProcessingException e) {
            log.error("Error serializing JSON setting", e);
            throw new RuntimeException("Failed to set JSON setting", e);
        }
    }
    
    /**
     * Delete a setting.
     */
    @CacheEvict(value = "settings", key = "#key")
    public void deleteSetting(String key) {
        settingsRepository.deleteByKey(key);
    }
    
    /**
     * Get all settings.
     */
    @Transactional(readOnly = true)
    public List<Settings> getAllSettings() {
        return settingsRepository.findAll();
    }
    
    /**
     * Get all public settings (non-sensitive).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPublicSettings() {
        Map<String, Object> publicSettings = new HashMap<>();
        settingsRepository.findAll().stream()
            .filter(s -> !s.isSensitive())
            .forEach(s -> publicSettings.put(s.getKey(), s.getValue()));
        return publicSettings;
    }
    
    /**
     * Initialize default settings if they don't exist.
     */
    @Transactional
    public void initializeDefaultSettings() {
        String[] defaults = {
            "site.name", "CTU Review Platform",
            "site.description", "Platform review giảng viên Trường Đại học Cần Thơ",
            "maintenance.mode", "false",
            "max.upload.size", "10485760", // 10MB
            "max.reviews.per.user.per.day", "5",
            "notification.email.enabled", "true"
        };
        
        for (int i = 0; i < defaults.length; i += 2) {
            String key = defaults[i];
            String value = defaults[i + 1];
            if (!settingsRepository.existsByKey(key)) {
                setSetting(key, value, "STRING", null);
            }
        }
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
