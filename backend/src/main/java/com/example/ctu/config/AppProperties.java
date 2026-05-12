package com.example.ctu.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
