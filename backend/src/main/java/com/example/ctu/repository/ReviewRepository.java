package com.example.ctu.repository;

import com.example.ctu.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByLecturer_IdAndApprovedOrderByCreatedAtDesc(Long lecturerId, boolean approved);
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    long countByAnonymousHashAndCreatedAtBetween(String anonymousHash, Instant start, Instant end);
    boolean existsByAnonymousHashAndLecturer_IdAndSemesterAndAcademicYear(String anonymousHash, Long lecturerId, String semester, String academicYear);
    List<Review> findByLecturer_IdOrderByCreatedAtDesc(Long lecturerId);

    @Query("select avg(r.ratingClarity) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageClarity(Long lecturerId);

    @Query("select avg(r.ratingFairness) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageFairness(Long lecturerId);

    @Query("select avg(r.ratingPressure) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averagePressure(Long lecturerId);

    @Query("select avg(r.ratingWorkload) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageWorkload(Long lecturerId);

    @Query("select avg(r.ratingSupport) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageSupport(Long lecturerId);

    long countByLecturer_IdAndApproved(Long lecturerId, boolean approved);
}
