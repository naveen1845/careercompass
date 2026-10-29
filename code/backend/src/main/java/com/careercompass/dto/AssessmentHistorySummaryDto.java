package com.careercompass.dto;

import java.time.LocalDateTime;

/**
 * Summary DTO representing a past assessment for the History Archive.
 */
public class AssessmentHistorySummaryDto {

    private Long id;
    private String studentName;
    private String educationLevel;
    private LocalDateTime createdAt;
    private String topCareerName;
    private int topMatchScore;
    private String dominantDimension;
    private int dominantDimensionScore;

    public AssessmentHistorySummaryDto() {
    }

    public AssessmentHistorySummaryDto(Long id, String studentName, String educationLevel,
                                       LocalDateTime createdAt, String topCareerName,
                                       int topMatchScore, String dominantDimension,
                                       int dominantDimensionScore) {
        this.id = id;
        this.studentName = studentName;
        this.educationLevel = educationLevel;
        this.createdAt = createdAt;
        this.topCareerName = topCareerName;
        this.topMatchScore = topMatchScore;
        this.dominantDimension = dominantDimension;
        this.dominantDimensionScore = dominantDimensionScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTopCareerName() {
        return topCareerName;
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
        return dominantDimension;
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
}
