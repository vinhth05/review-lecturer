package com.example.ctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.ctu.dto.review.ReviewDtos;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.Review;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.repository.LecturerRepository;
import com.example.ctu.repository.ReportRepository;
import com.example.ctu.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private LecturerRepository lecturerRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private HashService hashService;
    @Mock private ToxicFilterService toxicFilterService;
    @Mock private AppPropertiesFacade appPropertiesFacade;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void submitReview_setsApprovedToFalseByDefault() {
        // Arrange
        User user = User.builder()
                .studentCode("SV0001")
                .verified(true)
                .build();
        
        Lecturer lecturer = Lecturer.builder()
                .id(1L)
                .status(LecturerStatus.ACTIVE)
                .build();

        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                1L, 5, 5, 5, 5, 5, "Good lecturer", "Semester 1", "2023-2024"
        );

        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(hashService.anonymousHash("SV0001")).thenReturn("hashed-student-code");
        when(appPropertiesFacade.reviewRateLimitPerDay()).thenReturn(5);
        when(reviewRepository.countByAnonymousHashAndCreatedAtBetween(eq("hashed-student-code"), any(), any())).thenReturn(0L);
        when(reviewRepository.existsByAnonymousHashAndLecturer_IdAndSemesterAndAcademicYear(
                eq("hashed-student-code"), eq(1L), eq("Semester 1"), eq("2023-2024"))).thenReturn(false);
        when(reviewRepository.countByAnonymousHashAndLecturer_Id("hashed-student-code", 1L)).thenReturn(0L);
        when(toxicFilterService.containsToxicWord("Good lecturer")).thenReturn(false);
        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(lecturer));
        
        Review mockSavedReview = Review.builder().id(100L).approved(false).build();
        when(reviewRepository.save(any(Review.class))).thenReturn(mockSavedReview);

        // Act
        Review result = reviewService.submit(request);

        // Assert
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview.isApproved()).isFalse(); // MUST be pending approval
        assertThat(result.isApproved()).isFalse();
    }
}
