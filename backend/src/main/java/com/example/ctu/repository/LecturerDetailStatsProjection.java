package com.example.ctu.repository;

public interface LecturerDetailStatsProjection {
    Double getAverageClarity();
    Double getAverageFairness();
    Double getAveragePressure();
    Double getAverageWorkload();
    Double getAverageSupport();
    Long getReviewCount();
}
