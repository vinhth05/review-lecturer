package com.example.ctu.controller;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.service.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final CurrentUserService currentUserService;

    public StudentController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
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
}
