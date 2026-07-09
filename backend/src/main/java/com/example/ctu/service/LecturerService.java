package com.example.ctu.service;

import java.util.Comparator;
import java.util.List;
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

    public List<LecturerDtos.LecturerSummaryResponse> list(String facultyCode, String subjectCode) {
        List<Lecturer> lecturers;
        if (subjectCode != null && !subjectCode.isBlank()) {
            lecturers = lecturerRepository.findBySubject_CodeAndStatus(subjectCode, LecturerStatus.ACTIVE);
        } else if (facultyCode != null && !facultyCode.isBlank()) {
            lecturers = lecturerRepository.findByFaculty_CodeAndStatus(facultyCode, LecturerStatus.ACTIVE);
        } else {
            lecturers = lecturerRepository.findByStatus(LecturerStatus.ACTIVE);
        }
        return lecturers.stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(LecturerDtos.LecturerSummaryResponse::fullName))
                .collect(Collectors.toList());
    }

    public Page<LecturerDtos.LecturerSummaryResponse> listPage(String facultyCode, String subjectCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Lecturer> lecturers;
        if (subjectCode != null && !subjectCode.isBlank()) {
            lecturers = lecturerRepository.findBySubject_CodeAndStatus(subjectCode, LecturerStatus.ACTIVE, pageable);
        } else if (facultyCode != null && !facultyCode.isBlank()) {
            lecturers = lecturerRepository.findByFaculty_CodeAndStatus(facultyCode, LecturerStatus.ACTIVE, pageable);
        } else {
            lecturers = lecturerRepository.findByStatus(LecturerStatus.ACTIVE, pageable);
        }
        return lecturers.map(this::toSummary);
    }

    public LecturerDtos.LecturerDetailResponse detail(Long id) {
        Lecturer lecturer = lecturerRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên không tồn tại"));
        List<Review> reviews = reviewRepository.findByLecturer_IdAndApprovedOrderByCreatedAtDesc(id, true);
        double averageRating = overallAverage(reviews);
        return new LecturerDtos.LecturerDetailResponse(
                lecturer.getId(),
                lecturer.getLecturerCode(),
                lecturer.getFullName(),
                lecturer.getFaculty().getName(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getName(),
                lecturer.getStatus(),
            averageRating,
                average(reviews, Review::getRatingClarity),
                average(reviews, Review::getRatingFairness),
                average(reviews, Review::getRatingPressure),
                average(reviews, Review::getRatingWorkload),
                average(reviews, Review::getRatingSupport),
                reviews.size(),
                distribution(reviews),
                semesterComparisons(reviews),
                reviews.stream().limit(10).map(review -> new LecturerDtos.ReviewItem(
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
                )).toList()
        );
    }

    private LecturerDtos.LecturerSummaryResponse toSummary(Lecturer lecturer) {
        List<Review> reviews = reviewRepository.findByLecturer_IdAndApprovedOrderByCreatedAtDesc(lecturer.getId(), true);
        return new LecturerDtos.LecturerSummaryResponse(
                lecturer.getId(),
                lecturer.getLecturerCode(),
                lecturer.getFullName(),
                lecturer.getFaculty().getName(),
                lecturer.getSubject() == null ? null : lecturer.getSubject().getName(),
                lecturer.getStatus(),
                overallAverage(reviews),
                reviews.size()
        );
    }

    private double overallAverage(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToDouble(review ->
                (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0
        ).average().orElse(0);
    }

    private double average(List<Review> reviews, java.util.function.ToIntFunction<Review> extractor) {
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToInt(extractor).average().orElse(0);
    }

    private List<LecturerDtos.RatingDistributionItem> distribution(List<Review> reviews) {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(score -> new LecturerDtos.RatingDistributionItem(score, reviews.stream()
                        .mapToInt(review -> (int) Math.round((review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0))
                        .filter(value -> value == score)
                        .count()))
                .toList();
    }

    private List<LecturerDtos.SemesterComparisonItem> semesterComparisons(List<Review> reviews) {
        return reviews.stream()
                .collect(Collectors.groupingBy(review -> review.getSemester() + "|" + review.getAcademicYear()))
                .entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey()))
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|");
                    double avg = entry.getValue().stream().mapToDouble(review ->
                            (review.getRatingClarity() + review.getRatingFairness() + review.getRatingPressure() + review.getRatingWorkload() + review.getRatingSupport()) / 5.0
                    ).average().orElse(0);
                    return new LecturerDtos.SemesterComparisonItem(parts[0], parts[1], avg, entry.getValue().size());
                })
                .toList();
    }
}
