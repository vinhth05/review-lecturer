package com.example.ctu.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ctu.dto.lecturer.LecturerDtos;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Review;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReviewRepository;
import com.example.ctu.repository.LecturerRatingStats;
import com.example.ctu.repository.LecturerDetailStatsProjection;
import com.example.ctu.repository.RatingDistributionProjection;
import com.example.ctu.repository.SemesterComparisonProjection;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final ReviewRepository reviewRepository;

    public LecturerService(LecturerRepository lecturerRepository, ReviewRepository reviewRepository) {
        this.lecturerRepository = lecturerRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<LecturerDtos.LecturerSummaryResponse> list(String facultyCode, String subjectCode, String search) {
        List<Lecturer> lecturers = lecturerRepository.searchLecturers(facultyCode, subjectCode, search, LecturerStatus.ACTIVE);
        return mapToSummaryList(lecturers);
    }

    public Page<LecturerDtos.LecturerSummaryResponse> listPage(String facultyCode, String subjectCode, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Lecturer> lecturers = lecturerRepository.searchLecturers(facultyCode, subjectCode, search, LecturerStatus.ACTIVE, pageable);
        return mapToSummaryPage(lecturers, pageable);
    }

    public LecturerDtos.LecturerDetailResponse detail(Long id) {
        Lecturer lecturer = lecturerRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));

        // 1. Fetch detailed stats
        LecturerDetailStatsProjection stats = reviewRepository.findDetailStatsByLecturerId(id);
        long count = stats != null && stats.getReviewCount() != null ? stats.getReviewCount() : 0L;

        double averageRating = 0.0;
        double clarity = 0.0;
        double fairness = 0.0;
        double pressure = 0.0;
        double workload = 0.0;
        double support = 0.0;

        if (stats != null && count > 0) {
            clarity = stats.getAverageClarity() != null ? stats.getAverageClarity() : 0.0;
            fairness = stats.getAverageFairness() != null ? stats.getAverageFairness() : 0.0;
            pressure = stats.getAveragePressure() != null ? stats.getAveragePressure() : 0.0;
            workload = stats.getAverageWorkload() != null ? stats.getAverageWorkload() : 0.0;
            support = stats.getAverageSupport() != null ? stats.getAverageSupport() : 0.0;
            averageRating = (clarity + fairness + pressure + workload + support) / 5.0;
        }

        // 2. Fetch rating distribution
        List<RatingDistributionProjection> distProjections = reviewRepository.findDistributionByLecturerId(id);
        Map<Integer, Long> distMap = distProjections.stream()
                .collect(Collectors.toMap(RatingDistributionProjection::getScore, RatingDistributionProjection::getCount));
        List<LecturerDtos.RatingDistributionItem> distribution = IntStream.rangeClosed(1, 5)
                .mapToObj(score -> new LecturerDtos.RatingDistributionItem(score, distMap.getOrDefault(score, 0L)))
                .toList();

        // 3. Fetch semester comparisons
        List<SemesterComparisonProjection> semProjections = reviewRepository.findSemesterComparisonsByLecturerId(id);
        List<LecturerDtos.SemesterComparisonItem> semesterComparisons = semProjections.stream()
                .map(p -> new LecturerDtos.SemesterComparisonItem(
                        p.getSemester(),
                        p.getAcademicYear(),
                        p.getAverageRating() != null ? p.getAverageRating() : 0.0,
                        p.getReviewCount() != null ? p.getReviewCount() : 0L
                ))
                .sorted(Comparator.comparing(LecturerDtos.SemesterComparisonItem::semester).thenComparing(LecturerDtos.SemesterComparisonItem::academicYear))
                .toList();

        // 4. Fetch latest 10 reviews
        Pageable latestTen = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Review> latestReviewsPage = reviewRepository.findByLecturer_IdAndApproved(id, true, latestTen);
        List<LecturerDtos.ReviewItem> latestReviews = latestReviewsPage.getContent().stream()
                .map(review -> new LecturerDtos.ReviewItem(
                        review.getId(),
                        (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0,
                        review.getRatingClarity(),
                        review.getRatingFairness(),
                        review.getRatingPressure(),
                        review.getRatingWorkload(),
                        review.getRatingSupport(),
                        review.getComment(),
                        review.getSemester(),
                        review.getAcademicYear(),
                        review.getCreatedAt()
                )).toList();

        return new LecturerDtos.LecturerDetailResponse(
                lecturer.getId(),
                lecturer.getLecturerCode(),
                lecturer.getFullName(),
                lecturer.getFaculty().getName(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getName(),
                lecturer.getStatus(),
                averageRating,
                clarity,
                fairness,
                pressure,
                workload,
                support,
                count,
                distribution,
                semesterComparisons,
                latestReviews
        );
    }

    private List<LecturerDtos.LecturerSummaryResponse> mapToSummaryList(List<Lecturer> lecturers) {
        if (lecturers.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> lecturerIds = lecturers.stream().map(Lecturer::getId).toList();
        List<LecturerRatingStats> ratingStats = reviewRepository.findRatingStatsByLecturerIds(lecturerIds);
        Map<Long, LecturerRatingStats> statsMap = ratingStats.stream()
                .collect(Collectors.toMap(LecturerRatingStats::getLecturerId, stats -> stats));

        return lecturers.stream()
                .map(lecturer -> toSummaryWithStats(lecturer, statsMap.get(lecturer.getId())))
                .sorted(Comparator.comparing(LecturerDtos.LecturerSummaryResponse::fullName))
                .toList();
    }

    private Page<LecturerDtos.LecturerSummaryResponse> mapToSummaryPage(Page<Lecturer> lecturerPage, Pageable pageable) {
        List<Lecturer> lecturers = lecturerPage.getContent();
        if (lecturers.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Long> lecturerIds = lecturers.stream().map(Lecturer::getId).toList();
        List<LecturerRatingStats> ratingStats = reviewRepository.findRatingStatsByLecturerIds(lecturerIds);
        Map<Long, LecturerRatingStats> statsMap = ratingStats.stream()
                .collect(Collectors.toMap(LecturerRatingStats::getLecturerId, stats -> stats));

        return lecturerPage.map(lecturer -> toSummaryWithStats(lecturer, statsMap.get(lecturer.getId())));
    }

    private LecturerDtos.LecturerSummaryResponse toSummaryWithStats(Lecturer lecturer, LecturerRatingStats stats) {
        double avg = stats != null && stats.getAverageRating() != null ? stats.getAverageRating() : 0.0;
        long count = stats != null && stats.getReviewCount() != null ? stats.getReviewCount() : 0L;
        return new LecturerDtos.LecturerSummaryResponse(
                lecturer.getId(),
                lecturer.getLecturerCode(),
                lecturer.getFullName(),
                lecturer.getFaculty().getName(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getName(),
                lecturer.getStatus(),
                avg,
                count
        );
    }
}
