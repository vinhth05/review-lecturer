package com.example.ctu.service;

import com.example.ctu.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class AppPropertiesFacade {

    private final AppProperties properties;

    public AppPropertiesFacade(AppProperties properties) {
        this.properties = properties;
    }

    public int reviewRateLimitPerDay() {
        return properties.review().rateLimitPerDay();
    }
}
