package com.example.ctu.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Otp otp,
        Review review,
    Cors cors,
    Kafka kafka,
    Seed seed
) {
    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Otp(long ttlMinutes, String appName, String mailSubject) {
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

    public record Kafka(Topic topic) {
    }

    public record Topic(String otpEmail) {
    }

    public record Seed(Accounts accounts) {
    }

    public record Accounts(Account admin, Account student, Account superAdmin) {
    }

    public record Account(String studentCode, String fullName, String email, String password, String facultyCode) {
    }
}
