package com.example.ctu.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ctu.dto.lecturer.LecturerDtos;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.SubjectRepository;
import com.example.ctu.service.LecturerService;

@Controller
@RequestMapping("/lecturers")
public class LecturerController {

    private final LecturerService lecturerService;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    public LecturerController(LecturerService lecturerService,
                              FacultyRepository facultyRepository,
                              SubjectRepository subjectRepository) {
        this.lecturerService = lecturerService;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 48);
        Page<com.example.ctu.dto.lecturer.LecturerDtos.LecturerSummaryResponse> result =
            lecturerService.listPage(facultyCode, subjectCode, safePage, safeSize);

        model.addAttribute("lecturers", result.getContent());
        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("size", result.getSize());
        model.addAttribute("facultyCode", facultyCode);
        model.addAttribute("subjectCode", subjectCode);

        model.addAttribute("faculties", facultyRepository.findAllByOrderByNameAsc());
        if (facultyCode != null && !facultyCode.isBlank()) {
            model.addAttribute("subjects", subjectRepository.findByFaculty_CodeOrderByNameAsc(facultyCode));
        } else {
            model.addAttribute("subjects", subjectRepository.findAllByOrderByFaculty_NameAscNameAsc());
        }

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
