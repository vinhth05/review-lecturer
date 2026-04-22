package com.example.ctu.repository;

import com.example.ctu.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByReview_IdOrderByCreatedAtDesc(UUID reviewId);
    long countByReview_Id(UUID reviewId);
}
