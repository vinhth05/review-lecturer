package com.example.ctu.controller;

import com.example.ctu.dto.lecturer.LecturerDtos;
import com.example.ctu.service.LecturerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lecturers")
public class LecturerController {

    private final LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping
    public List<LecturerDtos.LecturerSummaryResponse> list(
            @RequestParam(required = false) String facultyCode,
            @RequestParam(required = false) String subjectCode) {
        return lecturerService.list(facultyCode, subjectCode);
    }

    @GetMapping("/{id}")
    public LecturerDtos.LecturerDetailResponse detail(@PathVariable UUID id) {
        return lecturerService.detail(id);
    }
}
