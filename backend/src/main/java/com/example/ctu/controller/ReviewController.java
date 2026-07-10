package com.example.ctu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Review;
import com.example.ctu.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
@PreAuthorize("hasRole('STUDENT')")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Long> submit(@Valid @RequestBody ReviewDtos.CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.submit(request).getId());
    }

    @PostMapping("/reports")
    public ResponseEntity<Long> report(@Valid @RequestBody ReviewDtos.CreateReportRequest request) {
        return ResponseEntity.ok(reviewService.report(request).getId());
    }

    @org.springframework.web.bind.annotation.GetMapping("/reviews/me")
    public ResponseEntity<java.util.List<ReviewDtos.MyReviewItem>> myReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }
}
