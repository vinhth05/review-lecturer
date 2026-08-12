package com.example.ctu.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.dto.common.ApiResponse;
import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Review;
import com.example.ctu.entity.Subject;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.service.AdminService;
import com.example.ctu.service.CtuLecturerImportService;
import com.example.ctu.service.CurrentUserService;
import com.example.ctu.service.ReviewService;
import com.example.ctu.service.ToxicKeywordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReviewService reviewService;
    private final AdminService adminService;
    private final ToxicKeywordService toxicKeywordService;
    private final CtuLecturerImportService ctuLecturerImportService;
    private final CurrentUserService currentUserService;

    public AdminController(ReviewService reviewService,
                           AdminService adminService,
                           ToxicKeywordService toxicKeywordService,
                           CtuLecturerImportService ctuLecturerImportService,
                           CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.adminService = adminService;
        this.toxicKeywordService = toxicKeywordService;
        this.ctuLecturerImportService = ctuLecturerImportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/reviews/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<AdminDtos.PendingReviewItem>> pendingReviews() {
        return ApiResponse.success(reviewService.pendingReviews());
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.ReviewItem>> reviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.listReviews(page, size));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.UserItem>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean verified) {
        return ApiResponse.success(adminService.listUsers(page, size, keyword, role, verified));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.UserItem>> updateUserRole(@PathVariable Long id,
                                                             @Valid @RequestBody AdminDtos.UpdateUserRoleRequest request) {
        User actor = currentUserService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserRole(id, request, actor)));
    }

    @PatchMapping("/reviews/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.moderate(id, true).getId()));
    }

    @PatchMapping("/reviews/{id}/moderation")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDtos.ModerationResult>> moderateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDtos.ModerateReviewCommand command) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.moderate(id, command.status(), command.reason(), command.expectedVersion())
        ));
    }

    @PatchMapping("/lecturers/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> hideLecturer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.hideLecturer(id).getId()));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.AdminStatisticResponse> statistics() {
        return ApiResponse.success(adminService.statistics());
    }

    @PostMapping("/faculties")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createFaculty(@Valid @RequestBody AdminDtos.CreateFacultyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createFaculty(request).getId()));
    }

    @GetMapping("/faculties")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.FacultyItem>> faculties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.listFaculties(page, size));
    }

    @PatchMapping("/faculties/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateFaculty(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateFacultyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateFaculty(id, request).getId()));
    }

    @DeleteMapping("/faculties/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        adminService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khoa thành công", null));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createSubject(@Valid @RequestBody AdminDtos.CreateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createSubject(request).getId()));
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.SubjectItem>> subjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.listSubjects(page, size));
    }

    @PatchMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateSubject(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateSubject(id, request).getId()));
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        adminService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bộ môn thành công", null));
    }

    @PostMapping("/lecturers")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createLecturer(@Valid @RequestBody AdminDtos.CreateLecturerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createLecturer(request).getId()));
    }

    @GetMapping("/lecturers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.LecturerItem>> lecturers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.listLecturers(page, size, keyword));
    }

    @PatchMapping("/lecturers/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateLecturer(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateLecturerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateLecturer(id, request).getId()));
    }

    @DeleteMapping("/lecturers/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLecturer(@PathVariable Long id) {
        adminService.deleteLecturer(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa giảng viên thành công", null));
    }

    @PostMapping("/lecturers/import/ctu")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.ImportCtuLecturersResponse>> importCtuLecturers(@RequestBody(required = false) AdminDtos.ImportCtuLecturersRequest request) {
        AdminDtos.ImportCtuLecturersRequest payload = request == null
                ? new AdminDtos.ImportCtuLecturersRequest(null, null)
                : request;
        return ResponseEntity.ok(ApiResponse.success(ctuLecturerImportService.importFromCtu(payload)));
    }

    @GetMapping("/toxic-keywords")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<AdminDtos.ToxicKeywordItem>> listToxicKeywords() {
        return ApiResponse.success(toxicKeywordService.getAll());
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDtos.PageResponse<AdminDtos.ReportItem>> reports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.listReports(page, size));
    }

    @PostMapping("/toxic-keywords")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.ToxicKeywordItem>> addToxicKeyword(@Valid @RequestBody AdminDtos.CreateToxicKeywordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(toxicKeywordService.add(request)));
    }

    @PatchMapping("/toxic-keywords/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.ToxicKeywordItem>> updateToxicKeyword(@PathVariable Long id,
                                                                         @Valid @RequestBody AdminDtos.UpdateToxicKeywordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(toxicKeywordService.update(id, request)));
    }

    @DeleteMapping("/toxic-keywords/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteToxicKeyword(@PathVariable Long id) {
        toxicKeywordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa từ khóa thành công", null));
    }

    @DeleteMapping("/reports/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        adminService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa báo cáo thành công", null));
    }

    @PatchMapping("/reports/{id}/resolution")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDtos.ReportResolutionResult>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDtos.ResolveReportCommand command) {
        return ResponseEntity.ok(ApiResponse.success(adminService.resolveReport(id, command)));
    }

    @PostMapping("/reports/bulk-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteReports(@RequestBody List<Long> ids) {
        adminService.bulkDeleteReports(ids);
        return ResponseEntity.ok(ApiResponse.success("Xóa loạt báo cáo thành công", null));
    }

    @PatchMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(@PathVariable Long id, @Valid @RequestBody AdminDtos.ResetUserPasswordRequest request) {
        String newPassword = adminService.resetUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(newPassword));
    }

    @PatchMapping("/reviews/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> rejectReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.moderate(id, false).getId()));
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
    }

    @PostMapping("/reviews/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteReviews(@RequestBody List<Long> ids) {
        adminService.bulkDeleteReviews(ids);
        return ResponseEntity.ok(ApiResponse.success("Xóa loạt đánh giá thành công", null));
    }

    @PostMapping("/reviews/bulk-approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bulkApprove(@RequestBody List<Long> ids) {
        adminService.bulkApproveReviews(ids);
        return ResponseEntity.ok(ApiResponse.success("Duyệt loạt đánh giá thành công", null));
    }

    @PatchMapping("/lecturers/{id}/unhide")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> unhideLecturer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.unhideLecturer(id).getId()));
    }

    @PatchMapping("/users/{id}/verified")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.UserItem>> setUserVerified(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.setUserVerified(id, true)));
    }

    @PatchMapping("/users/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.UserItem>> lockUser(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(adminService.lockUser(id, actor)));
    }

    @PatchMapping("/users/{id}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.UserItem>> unlockUser(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(adminService.unlockUser(id, actor)));
    }

    @PatchMapping("/users/{id}/unverify")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDtos.UserItem>> unsetUserVerified(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.setUserVerified(id, false)));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        adminService.deleteUser(id, actor);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }

    @GetMapping("/export/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> exportUsers() {
        String csv = adminService.exportUsersCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=users.csv")
                .body(csv);
    }

    @GetMapping("/export/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> exportReviews(@RequestParam(required = false) Boolean approved) {
        String csv = adminService.exportReviewsCsv(approved);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=reviews.csv")
                .body(csv);
    }
}
