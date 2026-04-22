package com.example.ctu.repository;

import com.example.ctu.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByLecturer_IdAndApprovedOrderByCreatedAtDesc(UUID lecturerId, boolean approved);
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    long countByAnonymousHashAndCreatedAtBetween(String anonymousHash, Instant start, Instant end);
    boolean existsByAnonymousHashAndLecturer_IdAndSemesterAndAcademicYear(String anonymousHash, UUID lecturerId, String semester, String academicYear);
    List<Review> findByLecturer_IdOrderByCreatedAtDesc(UUID lecturerId);

    @Query("select avg(r.ratingClarity) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageClarity(UUID lecturerId);

    @Query("select avg(r.ratingFairness) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageFairness(UUID lecturerId);

    @Query("select avg(r.ratingPressure) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averagePressure(UUID lecturerId);

    @Query("select avg(r.ratingWorkload) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageWorkload(UUID lecturerId);

    @Query("select avg(r.ratingSupport) from Review r where r.lecturer.id = :lecturerId and r.approved = true")
    Double averageSupport(UUID lecturerId);

    long countByLecturer_IdAndApproved(UUID lecturerId, boolean approved);
}
