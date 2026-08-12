package com.example.ctu.repository;

import com.example.ctu.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.example.ctu.entity.enums.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReview_IdOrderByCreatedAtDesc(Long reviewId);
    long countByReview_Id(Long reviewId);
    long countByReview_IdAndStatus(Long reviewId, ReportStatus status);
    boolean existsByReview_IdAndReporterHashAndStatus(Long reviewId, String reporterHash, ReportStatus status);

    @Modifying
    @Query("delete from Report r where r.review.id = :reviewId")
    void deleteByReviewId(Long reviewId);

    @EntityGraph(attributePaths = {"review", "review.lecturer"})
    @Query("select r from Report r")
    Page<Report> findAllWithReviewAndLecturer(Pageable pageable);

    @EntityGraph(attributePaths = {"review", "review.lecturer"})
    @Query("select report from Report report where report.status = :status")
    Page<Report> findByStatusWithReviewAndLecturer(ReportStatus status, Pageable pageable);
}
