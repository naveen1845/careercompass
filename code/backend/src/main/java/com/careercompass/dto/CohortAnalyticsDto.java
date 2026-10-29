package com.careercompass.dto;

import java.util.Map;

/**
 * Cohort Benchmarking Analytics DTO aggregating historical data across all student assessments.
 */
public class CohortAnalyticsDto {

    private long totalAssessments;
    private String mostCommonCareer;
    private int mostCommonCareerCount;
    private Map<String, Double> dimensionAverages;
    private Map<String, Long> careerDistribution;

    public CohortAnalyticsDto() {
    }

    public CohortAnalyticsDto(long totalAssessments, String mostCommonCareer, int mostCommonCareerCount,
                              Map<String, Double> dimensionAverages, Map<String, Long> careerDistribution) {
        this.totalAssessments = totalAssessments;
        this.mostCommonCareer = mostCommonCareer;
        this.mostCommonCareerCount = mostCommonCareerCount;
        this.dimensionAverages = dimensionAverages;
        this.careerDistribution = careerDistribution;
    }

    public long getTotalAssessments() {
        return totalAssessments;
    }

    public void setTotalAssessments(long totalAssessments) {
        this.totalAssessments = totalAssessments;
    }

    public String getMostCommonCareer() {
        return mostCommonCareer;
    }

    public void setMostCommonCareer(String mostCommonCareer) {
        this.mostCommonCareer = mostCommonCareer;
    }

    public int getMostCommonCareerCount() {
        return mostCommonCareerCount;
    }

    public void setMostCommonCareerCount(int mostCommonCareerCount) {
        this.mostCommonCareerCount = mostCommonCareerCount;
    }

    public Map<String, Double> getDimensionAverages() {
        return dimensionAverages;
    }

    public void setDimensionAverages(Map<String, Double> dimensionAverages) {
        this.dimensionAverages = dimensionAverages;
    }

    public Map<String, Long> getCareerDistribution() {
        return careerDistribution;
    }

    public void setCareerDistribution(Map<String, Long> careerDistribution) {
        this.careerDistribution = careerDistribution;
    }
}
