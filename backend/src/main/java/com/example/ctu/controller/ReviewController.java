package com.example.ctu.controller;

import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Report;
import com.example.ctu.entity.Review;
import com.example.ctu.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> submit(@Valid @RequestBody ReviewDtos.CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.submit(request));
    }

    @PostMapping("/reports")
    public ResponseEntity<Report> report(@Valid @RequestBody ReviewDtos.CreateReportRequest request) {
        return ResponseEntity.ok(reviewService.report(request));
    }
}
