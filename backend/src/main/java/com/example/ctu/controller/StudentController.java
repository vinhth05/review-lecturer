package com.example.ctu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.service.AuthService;
import com.example.ctu.service.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final CurrentUserService currentUserService;
    private final AuthService authService;

    public StudentController(CurrentUserService currentUserService, AuthService authService) {
        this.currentUserService = currentUserService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public AuthDtos.ProfileResponse me() {
        var user = currentUserService.requireCurrentUser();
        return new AuthDtos.ProfileResponse(
                user.getStudentCode(),
                user.getFullName(),
                user.getEmail(),
                user.getFaculty().getName(),
                user.getRole(),
                user.isVerified()
        );
    }

    @PutMapping("/me")
    public AuthDtos.ProfileResponse updateMe(@Valid @RequestBody AuthDtos.UpdateProfileRequest request) {
        var user = currentUserService.requireCurrentUser();
        return authService.updateProfile(user.getId(), request);
    }

    @PostMapping("/me/change-password")
    public String changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        var user = currentUserService.requireCurrentUser();
        return authService.changePassword(user.getId(), request);
    }
}
