package com.example.ctu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Otp otp,
        Review review,
    Cors cors
) {
    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Otp(long ttlMinutes) {
    }

    public record Review(int rateLimitPerDay, String secretKey, List<String> toxicKeywords) {
        public Review {
            if (toxicKeywords == null) {
                toxicKeywords = new ArrayList<>();
            }
        }
    }

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            if (allowedOrigins == null) {
                allowedOrigins = new ArrayList<>();
            }
        }
    }
}
