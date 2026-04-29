package com.example.ctu.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ctu.dto.lecturer.LecturerDtos;
import com.example.ctu.service.LecturerService;

@Controller
@RequestMapping("/lecturers")
public class LecturerController {

    private final LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LecturerDtos.LecturerSummaryResponse> listJson(
            @RequestParam(required = false) String facultyCode,
            @RequestParam(required = false) String subjectCode) {
        return lecturerService.list(facultyCode, subjectCode);
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listPage(
            @RequestParam(required = false) String facultyCode,
            @RequestParam(required = false) String subjectCode,
            Model model) {
        model.addAttribute("lecturers", lecturerService.list(facultyCode, subjectCode));
        model.addAttribute("facultyCode", facultyCode);
        model.addAttribute("subjectCode", subjectCode);
        return "lecturers";
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LecturerDtos.LecturerDetailResponse detailJson(@PathVariable Long id) {
        return lecturerService.detail(id);
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String detailPage(@PathVariable Long id, Model model) {
        model.addAttribute("lecturer", lecturerService.detail(id));
        return "lecturer-detail";
    }
}
