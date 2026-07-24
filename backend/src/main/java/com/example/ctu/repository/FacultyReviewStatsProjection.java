package com.example.ctu.repository;

public interface FacultyReviewStatsProjection {
    Long getFacultyId();
    Long getReviewCount();
    Double getAverageRating();
}
