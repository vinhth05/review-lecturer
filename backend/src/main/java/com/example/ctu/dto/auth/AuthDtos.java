package com.example.ctu.dto.auth;

import com.example.ctu.entity.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String studentCode,
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự") String password,
            @NotBlank String confirmPassword,
            @NotNull Long facultyId
    ) {
    }

    public record VerifyRequest(@Email @NotBlank String email, @NotBlank String otp) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record ForgotPasswordRequest(@Email @NotBlank String email) {
    }

    public record ResetPasswordRequest(
            @Email @NotBlank String email,
            @NotBlank String otp,
            @NotBlank @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự") String newPassword,
            @NotBlank String confirmPassword
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự") String newPassword,
            @NotBlank String confirmPassword
    ) {
    }

    public record UpdateProfileRequest(
            @NotBlank String fullName,
            @NotNull Long facultyId
    ) {
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
