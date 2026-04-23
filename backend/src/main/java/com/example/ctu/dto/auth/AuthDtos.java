package com.example.ctu.dto.auth;

import com.example.ctu.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String studentCode,
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotNull Long facultyId
    ) {
    }

    public record VerifyRequest(@Email @NotBlank String email, @NotBlank String otp) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record AuthResponse(String token, Role role, boolean verified, String fullName, String facultyName) {
    }

    public record ProfileResponse(
            String studentCode,
            String fullName,
            String email,
            String facultyName,
            Role role,
            boolean verified
    ) {
    }
}
