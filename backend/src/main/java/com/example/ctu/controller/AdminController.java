package com.example.ctu.controller;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Subject;
import com.example.ctu.entity.Review;
import com.example.ctu.service.AdminService;
import com.example.ctu.service.CtuLecturerImportService;
import com.example.ctu.service.ReviewService;
import com.example.ctu.service.ToxicKeywordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final ReviewService reviewService;
    private final AdminService adminService;
    private final ToxicKeywordService toxicKeywordService;
    private final CtuLecturerImportService ctuLecturerImportService;

    public AdminController(ReviewService reviewService,
                           AdminService adminService,
                           ToxicKeywordService toxicKeywordService,
                           CtuLecturerImportService ctuLecturerImportService) {
        this.reviewService = reviewService;
        this.adminService = adminService;
        this.toxicKeywordService = toxicKeywordService;
        this.ctuLecturerImportService = ctuLecturerImportService;
    }

    @GetMapping("/reviews/pending")
    public List<AdminDtos.PendingReviewItem> pendingReviews() {
        return reviewService.pendingReviews();
    }

    @PatchMapping("/reviews/{id}/approve")
    public ResponseEntity<Review> approveReview(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.moderate(id, true));
    }

    @PatchMapping("/lecturers/{id}/hide")
    public ResponseEntity<Lecturer> hideLecturer(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.hideLecturer(id));
    }

    @GetMapping("/statistics")
    public AdminDtos.AdminStatisticResponse statistics() {
        return adminService.statistics();
    }

    @PostMapping("/faculties")
    public ResponseEntity<Faculty> createFaculty(@Valid @RequestBody AdminDtos.CreateFacultyRequest request) {
        return ResponseEntity.ok(adminService.createFaculty(request));
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody AdminDtos.CreateSubjectRequest request) {
        return ResponseEntity.ok(adminService.createSubject(request));
    }

    @PostMapping("/lecturers")
    public ResponseEntity<Lecturer> createLecturer(@Valid @RequestBody AdminDtos.CreateLecturerRequest request) {
        return ResponseEntity.ok(adminService.createLecturer(request));
    }

    @PostMapping("/lecturers/import/ctu")
    public ResponseEntity<AdminDtos.ImportCtuLecturersResponse> importCtuLecturers(@RequestBody(required = false) AdminDtos.ImportCtuLecturersRequest request) {
        AdminDtos.ImportCtuLecturersRequest payload = request == null
                ? new AdminDtos.ImportCtuLecturersRequest(null, null)
                : request;
        return ResponseEntity.ok(ctuLecturerImportService.importFromCtu(payload));
    }

    @GetMapping("/toxic-keywords")
    public List<AdminDtos.ToxicKeywordItem> listToxicKeywords() {
        return toxicKeywordService.getAll();
    }

    @PostMapping("/toxic-keywords")
    public ResponseEntity<AdminDtos.ToxicKeywordItem> addToxicKeyword(@Valid @RequestBody AdminDtos.CreateToxicKeywordRequest request) {
        return ResponseEntity.ok(toxicKeywordService.add(request));
    }

    @DeleteMapping("/toxic-keywords/{id}")
    public ResponseEntity<Void> deleteToxicKeyword(@PathVariable UUID id) {
        toxicKeywordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
