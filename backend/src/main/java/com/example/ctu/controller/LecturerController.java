package com.example.ctu.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.lecturer.LecturerDtos;
import com.example.ctu.service.LecturerService;

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

    @GetMapping("/page")
    public Page<LecturerDtos.LecturerSummaryResponse> listPage(
            @RequestParam(required = false) String facultyCode,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 48);
        return lecturerService.listPage(facultyCode, subjectCode, safePage, safeSize);
    }

    @GetMapping("/{id}")
    public LecturerDtos.LecturerDetailResponse detail(@PathVariable Long id) {
        return lecturerService.detail(id);
    }
}
