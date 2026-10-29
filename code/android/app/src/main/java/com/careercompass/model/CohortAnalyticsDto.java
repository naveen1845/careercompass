package com.careercompass.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Map;

public class CohortAnalyticsDto implements Serializable {

    @SerializedName("totalAssessments")
    private long totalAssessments;

    @SerializedName("mostCommonCareer")
    private String mostCommonCareer;

    @SerializedName("mostCommonCareerCount")
    private int mostCommonCareerCount;

    @SerializedName("dimensionAverages")
    private Map<String, Double> dimensionAverages;

    @SerializedName("careerDistribution")
    private Map<String, Long> careerDistribution;

    public CohortAnalyticsDto() {
    }

    public long getTotalAssessments() {
        return totalAssessments;
    }

    public void setTotalAssessments(long totalAssessments) {
        this.totalAssessments = totalAssessments;
    }

    public String getMostCommonCareer() {
        return mostCommonCareer != null ? mostCommonCareer : "None";
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
