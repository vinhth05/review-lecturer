package com.example.ctu.repository;

import com.example.ctu.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReview_IdOrderByCreatedAtDesc(Long reviewId);
    long countByReview_Id(Long reviewId);
}
