package com.example.ctu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Subject;
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
    public List<Faculty> faculties() {
        return facultyRepository.findAllByOrderByNameAsc();
    }

    @GetMapping("/subjects")
    public List<Subject> subjects(@RequestParam(required = false) String facultyCode) {
        if (facultyCode == null || facultyCode.isBlank()) {
            return subjectRepository.findAllByOrderByFaculty_NameAscNameAsc();
        }
        return subjectRepository.findByFaculty_CodeOrderByNameAsc(facultyCode);
    }
}