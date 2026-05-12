package com.example.ctu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.service.AuthService;

import jakarta.validation.Valid;

/**
 * REST Controller cho cac endpoint xac thuc.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Dang ky nguoi dung moi.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Xac thuc email bang OTP.
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthDtos.AuthResponse> verify(@Valid @RequestBody AuthDtos.VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Yeu cau dat lai mat khau.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        String result = authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(result);
    }

    /**
     * Dat lai mat khau bang OTP.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
