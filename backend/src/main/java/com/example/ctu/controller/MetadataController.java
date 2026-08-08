package com.example.ctu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.common.ApiResponse;
import com.example.ctu.dto.metadata.MetadataDtos;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.SubjectRepository;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    public MetadataController(FacultyRepository facultyRepository, SubjectRepository subjectRepository) {
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/faculties")
    public ApiResponse<List<MetadataDtos.FacultyResponse>> faculties() {
        List<MetadataDtos.FacultyResponse> list = facultyRepository.findAllByOrderByNameAsc().stream()
                .map(f -> new MetadataDtos.FacultyResponse(f.getId(), f.getName(), f.getCode()))
                .toList();
        return ApiResponse.success(list);
    }

    @GetMapping("/subjects")
    public ApiResponse<List<MetadataDtos.SubjectResponse>> subjects(@RequestParam(required = false) String facultyCode) {
        var subjects = (facultyCode == null || facultyCode.isBlank())
                ? subjectRepository.findAllByOrderByFaculty_NameAscNameAsc()
                : subjectRepository.findByFaculty_CodeOrderByNameAsc(facultyCode);

        List<MetadataDtos.SubjectResponse> list = subjects.stream()
                .map(s -> new MetadataDtos.SubjectResponse(
                        s.getId(),
                        s.getName(),
                        s.getCode(),
                        s.getFaculty() != null ? s.getFaculty().getId() : null,
                        s.getFaculty() != null ? s.getFaculty().getCode() : null,
                        s.getFaculty() != null ? s.getFaculty().getName() : null
                ))
                .toList();
        return ApiResponse.success(list);
    }
}