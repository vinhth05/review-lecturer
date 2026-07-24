package com.example.ctu.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ctu.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByLecturer_IdAndApprovedOrderByCreatedAtDesc(Long lecturerId, boolean approved);
    Page<Review> findByLecturer_IdAndApproved(Long lecturerId, boolean approved, Pageable pageable);
    
    @EntityGraph(attributePaths = {"lecturer", "lecturer.faculty"})
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    
    List<Review> findByAnonymousHashOrderByCreatedAtDesc(String anonymousHash);
    long countByAnonymousHashAndCreatedAtBetween(String anonymousHash, Instant start, Instant end);
    long countByAnonymousHashAndLecturer_Id(String anonymousHash, Long lecturerId);
    boolean existsByAnonymousHashAndLecturer_IdAndSemesterAndAcademicYear(String anonymousHash, Long lecturerId, String semester, String academicYear);
    
    @EntityGraph(attributePaths = {"lecturer", "lecturer.faculty"})
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
    long countByLecturer_Id(Long lecturerId);
    long countByApprovedTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM reviews r WHERE CAST(r.created_at AS DATE) = CAST(GETDATE() AS DATE)", nativeQuery = true)
    long countReviewsToday();

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM reviews r WHERE MONTH(r.created_at) = MONTH(GETDATE()) AND YEAR(r.created_at) = YEAR(GETDATE())", nativeQuery = true)
    long countReviewsThisMonth();

    long countByApprovedFalse();

    @EntityGraph(attributePaths = {"lecturer", "lecturer.faculty"})
    @Query("select r from Review r")
    Page<Review> findAllWithLecturerAndFaculty(Pageable pageable);

    @Query("select r.lecturer.id as lecturerId, " +
           "avg((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0) as averageRating, " +
           "count(r.id) as reviewCount " +
           "from Review r " +
           "where r.approved = true and r.lecturer.id in :lecturerIds " +
           "group by r.lecturer.id")
    List<LecturerRatingStats> findRatingStatsByLecturerIds(List<Long> lecturerIds);

    @Query("select " +
           "avg(r.ratingClarity) as averageClarity, " +
           "avg(r.ratingFairness) as averageFairness, " +
           "avg(r.ratingPressure) as averagePressure, " +
           "avg(r.ratingWorkload) as averageWorkload, " +
           "avg(r.ratingSupport) as averageSupport, " +
           "count(r.id) as reviewCount " +
           "from Review r " +
           "where r.lecturer.id = :lecturerId and r.approved = true")
    LecturerDetailStatsProjection findDetailStatsByLecturerId(Long lecturerId);

    @Query("select cast(round((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0, 0) as int) as score, " +
           "count(r.id) as count " +
           "from Review r " +
           "where r.lecturer.id = :lecturerId and r.approved = true " +
           "group by round((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0, 0)")
    List<RatingDistributionProjection> findDistributionByLecturerId(Long lecturerId);

    @Query("select r.semester as semester, " +
           "r.academicYear as academicYear, " +
           "avg((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0) as averageRating, " +
           "count(r.id) as reviewCount " +
           "from Review r " +
           "where r.lecturer.id = :lecturerId and r.approved = true " +
           "group by r.semester, r.academicYear")
    List<SemesterComparisonProjection> findSemesterComparisonsByLecturerId(Long lecturerId);

    @Query("select r.lecturer.id as lecturerId, " +
           "r.lecturer.fullName as lecturerName, " +
           "r.lecturer.faculty.name as facultyName, " +
           "avg((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0) as averageRating, " +
           "count(r.id) as reviewCount " +
           "from Review r " +
           "where r.approved = true " +
           "group by r.lecturer.id, r.lecturer.fullName, r.lecturer.faculty.name " +
           "order by avg((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0) desc")
    List<TopLecturerProjection> findTopLecturers(Pageable pageable);

    @Query("select r.review.lecturer.id as lecturerId, " +
           "r.review.lecturer.fullName as lecturerName, " +
           "r.review.lecturer.faculty.name as facultyName, " +
           "avg((r.review.ratingClarity + r.review.ratingFairness + r.review.ratingPressure + r.review.ratingWorkload + r.review.ratingSupport) / 5.0) as averageRating, " +
           "count(r.id) as reviewCount " + // reviewCount stores report count
           "from Report r " +
           "group by r.review.lecturer.id, r.review.lecturer.fullName, r.review.lecturer.faculty.name " +
           "order by count(r.id) desc")
    List<TopLecturerProjection> findTopReportedLecturers(Pageable pageable);

    @Query("select r.lecturer.faculty.id as facultyId, " +
           "count(r.id) as reviewCount, " +
           "avg((r.ratingClarity + r.ratingFairness + r.ratingPressure + r.ratingWorkload + r.ratingSupport) / 5.0) as averageRating " +
           "from Review r " +
           "where r.approved = true " +
           "group by r.lecturer.faculty.id")
    List<FacultyReviewStatsProjection> findReviewStatsByFaculty();

    @Query("select r.lecturer.faculty.id as facultyId, " +
           "count(r.id) as pendingReviewCount " +
           "from Review r " +
           "where r.approved = false " +
           "group by r.lecturer.faculty.id")
    List<FacultyPendingCountProjection> findPendingCountByFaculty();
}
