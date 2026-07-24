package com.example.ctu.repository;

public interface TopLecturerProjection {
    Long getLecturerId();
    String getLecturerName();
    String getFacultyName();
    Double getAverageRating();
    Long getReviewCount();
}
