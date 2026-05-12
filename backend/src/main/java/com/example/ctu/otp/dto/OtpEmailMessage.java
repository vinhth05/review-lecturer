package com.example.ctu.otp.dto;

import java.time.Instant;

public record OtpEmailMessage(String email, String otp, Instant expiresAt, String appName) {
}
