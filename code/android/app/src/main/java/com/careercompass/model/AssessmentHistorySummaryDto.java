package com.careercompass.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AssessmentHistorySummaryDto implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("studentName")
    private String studentName;

    @SerializedName("educationLevel")
    private String educationLevel;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("topCareerName")
    private String topCareerName;

    @SerializedName("topMatchScore")
    private int topMatchScore;

    @SerializedName("dominantDimension")
    private String dominantDimension;

    @SerializedName("dominantDimensionScore")
    private int dominantDimensionScore;

    public AssessmentHistorySummaryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName != null ? studentName : "Student";
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEducationLevel() {
        return educationLevel != null ? educationLevel : "";
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTopCareerName() {
        return topCareerName != null ? topCareerName : "Career Profile";
    }

    public void setTopCareerName(String topCareerName) {
        this.topCareerName = topCareerName;
    }

    public int getTopMatchScore() {
        return topMatchScore;
    }

    public void setTopMatchScore(int topMatchScore) {
        this.topMatchScore = topMatchScore;
    }

    public String getDominantDimension() {
        return dominantDimension != null ? dominantDimension : "Analytical Thinking";
    }

    public void setDominantDimension(String dominantDimension) {
        this.dominantDimension = dominantDimension;
    }

    public int getDominantDimensionScore() {
        return dominantDimensionScore;
    }

    public void setDominantDimensionScore(int dominantDimensionScore) {
        this.dominantDimensionScore = dominantDimensionScore;
    }

    public String getFormattedDate() {
        if (createdAt == null || createdAt.isEmpty()) return "Recent Assessment";
        try {
            // e.g. 2026-10-11T20:09:09
            String cleaned = createdAt.replace("T", " ");
            if (cleaned.length() >= 16) {
                return cleaned.substring(0, 16);
            }
            return cleaned;
        } catch (Exception e) {
            return createdAt;
        }
    }
}
