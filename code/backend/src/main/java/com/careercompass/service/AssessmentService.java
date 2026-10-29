package com.careercompass.service;

import com.careercompass.dto.AssessmentHistorySummaryDto;
import com.careercompass.dto.AssessmentResultResponse;
import com.careercompass.dto.AssessmentSubmissionRequest;
import com.careercompass.dto.CareerProfileDto;
import com.careercompass.dto.CareerRecommendationDto;
import com.careercompass.dto.CohortAnalyticsDto;
import com.careercompass.dto.QuestionAnswerDto;
import com.careercompass.dto.TraitScoreDto;
import com.careercompass.model.AssessmentResult;
import com.careercompass.model.CareerProfile;
import com.careercompass.model.CareerRecommendation;
import com.careercompass.model.Dimension;
import com.careercompass.model.Question;
import com.careercompass.model.Response;
import com.careercompass.model.Student;
import com.careercompass.model.TraitScore;
import com.careercompass.repository.AssessmentResultRepository;
import com.careercompass.repository.CareerProfileRepository;
import com.careercompass.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Core Assessment Service coordinating:
 * - Assessment question retrieval
 * - Response validation & mapping
 * - Dimension scoring (via ScoreCalculator and ScoringStrategy)
 * - Rule-based career ranking (via RecommendationEngine)
 * - Relational persistence in MySQL database
 */
@Service
public class AssessmentService {

    private final QuestionRepository questionRepository;
    private final CareerProfileRepository careerProfileRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final ScoreCalculator scoreCalculator;
    private final RecommendationEngine recommendationEngine;

    @Autowired
    public AssessmentService(QuestionRepository questionRepository,
                             CareerProfileRepository careerProfileRepository,
                             AssessmentResultRepository assessmentResultRepository,
                             ScoreCalculator scoreCalculator,
                             RecommendationEngine recommendationEngine) {
        this.questionRepository = questionRepository;
        this.careerProfileRepository = careerProfileRepository;
        this.assessmentResultRepository = assessmentResultRepository;
        this.scoreCalculator = scoreCalculator;
        this.recommendationEngine = recommendationEngine;
    }

    /**
     * Retrieves all 15 assessment questions sorted by display order.
     */
    public List<Question> getAllQuestions() {
        return questionRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Retrieves all predefined career profiles as DTOs.
     */
    public List<CareerProfileDto> getAllCareerProfiles() {
        List<CareerProfile> profiles = careerProfileRepository.findAll();
        List<CareerProfileDto> dtos = new ArrayList<>();
        for (CareerProfile p : profiles) {
            Map<String, Integer> weights = new LinkedHashMap<>();
            weights.put("Analytical Thinking", p.getWeightAnalytical());
            weights.put("Creativity", p.getWeightCreativity());
            weights.put("Social Orientation", p.getWeightSocial());
            weights.put("Leadership and Initiative", p.getWeightLeadership());
            weights.put("Practical and Hands-on Orientation", p.getWeightPractical());

            dtos.add(new CareerProfileDto(
                    p.getId(),
                    p.getCareerName(),
                    p.getDescription(),
                    p.getRelevantSkills(),
                    p.getMatchingExplanation(),
                    p.getLearningActivities(),
                    weights
            ));
        }
        return dtos;
    }

    /**
     * Submits assessment answers, computes scores, generates career recommendations,
     * and persists the result.
     */
    @Transactional
    public AssessmentResultResponse submitAssessment(AssessmentSubmissionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Submission request cannot be null.");
        }

        // 1. Encapsulate and validate student details
        Student student = new Student(request.getStudentName(), request.getEducationLevel());

        // 2. Fetch reference questions from database
        List<Question> questions = questionRepository.findAllByOrderByDisplayOrderAsc();
        if (questions == null || questions.isEmpty()) {
            throw(new IllegalStateException("No assessment questions found in the database. Ensure seed data is initialized."));
        }

        // 3. Map request DTOs to domain Response entities
        List<QuestionAnswerDto> answersDto = request.getAnswers();
        if (answersDto == null || answersDto.size() != questions.size()) {
            throw new IllegalArgumentException("Assessment requires answering exactly " + questions.size() +
                    " questions. Received: " + (answersDto == null ? 0 : answersDto.size()));
        }

        List<Response> domainResponses = new ArrayList<>();
        for (QuestionAnswerDto dto : answersDto) {
            domainResponses.add(new Response(dto.getQuestionId(), dto.getRating()));
        }

        // 4. Calculate trait scores using polymorphic scoring strategy
        List<TraitScore> traitScores = scoreCalculator.computeTraitScores(domainResponses, questions);

        // 5. Fetch career profiles from database and rank them
        List<CareerProfile> careerProfiles = careerProfileRepository.findAll();
        if (careerProfiles == null || careerProfiles.isEmpty()) {
            throw new IllegalStateException("No career profiles found in the database. Ensure seed data is initialized.");
        }

        List<CareerRecommendation> recommendations = recommendationEngine.recommendCareers(traitScores, careerProfiles);

        // 6. Build and persist AssessmentResult
        AssessmentResult result = new AssessmentResult(student, domainResponses, traitScores, recommendations);
        AssessmentResult savedResult = assessmentResultRepository.save(result);

        // 7. Map to response DTO
        return mapToResponseDto(savedResult);
    }

    /**
     * Retrieves an existing assessment result by ID.
     */
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessmentResultById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Assessment result ID cannot be null.");
        }
        AssessmentResult result = assessmentResultRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Assessment result not found for ID: " + id));

        return mapToResponseDto(result);
    }

    /**
     * Converts domain AssessmentResult entity to AssessmentResultResponse DTO.
     */
    private AssessmentResultResponse mapToResponseDto(AssessmentResult result) {
        List<TraitScoreDto> scoreDtos = new ArrayList<>();
        for (TraitScore ts : result.getTraitScores()) {
            scoreDtos.add(new TraitScoreDto(
                    ts.getDimension(),
                    ts.getDimension().getDisplayName(),
                    ts.getDimension().getDescription(),
                    ts.getRawScore(),
                    ts.getNormalizedScore()
            ));
        }

        List<CareerRecommendationDto> recDtos = new ArrayList<>();
        for (CareerRecommendation rec : result.getRecommendations()) {
            recDtos.add(new CareerRecommendationDto(
                    rec.getCareerId(),
                    rec.getCareerName(),
                    rec.getMatchScore(),
                    rec.getRankOrder(),
                    rec.getDescription(),
                    rec.getRelevantSkills(),
                    rec.getMatchingExplanation(),
                    rec.getLearningActivities()
            ));
        }

        return new AssessmentResultResponse(
                result.getId(),
                result.getStudent().getName(),
                result.getStudent().getEducationLevel(),
                result.getCreatedAt(),
                scoreDtos,
                recDtos
        );
    }

    /**
     * Retrieves all past assessments as summary records, sorted by newest first.
     */
    @Transactional(readOnly = true)
    public List<AssessmentHistorySummaryDto> getAssessmentHistory() {
        List<AssessmentResult> results = assessmentResultRepository.findAllByOrderByCreatedAtDesc();
        List<AssessmentHistorySummaryDto> dtos = new ArrayList<>();

        for (AssessmentResult r : results) {
            String topCareerName = "N/A";
            int topMatchScore = 0;
            if (r.getRecommendations() != null && !r.getRecommendations().isEmpty()) {
                CareerRecommendation topRec = r.getRecommendations().get(0);
                topCareerName = topRec.getCareerName();
                topMatchScore = topRec.getMatchScore();
            }

            String dominantDimension = "N/A";
            int dominantScore = 0;
            if (r.getTraitScores() != null) {
                for (TraitScore ts : r.getTraitScores()) {
                    if (ts.getNormalizedScore() > dominantScore) {
                        dominantScore = ts.getNormalizedScore();
                        dominantDimension = ts.getDimension().getDisplayName();
                    }
                }
            }

            dtos.add(new AssessmentHistorySummaryDto(
                    r.getId(),
                    r.getStudent() != null ? r.getStudent().getName() : "Anonymous",
                    r.getStudent() != null ? r.getStudent().getEducationLevel() : "",
                    r.getCreatedAt(),
                    topCareerName,
                    topMatchScore,
                    dominantDimension,
                    dominantScore
            ));
        }

        return dtos;
    }

    /**
     * Computes cohort benchmarking analytics across all recorded student evaluations.
     */
    @Transactional(readOnly = true)
    public CohortAnalyticsDto getCohortAnalytics() {
        List<AssessmentResult> results = assessmentResultRepository.findAll();
        long total = results.size();
        if (total == 0) {
            return new CohortAnalyticsDto(0, "None", 0, Map.of(), Map.of());
        }

        Map<Dimension, Integer> sumMap = new LinkedHashMap<>();
        Map<Dimension, Integer> countMap = new LinkedHashMap<>();
        for (Dimension d : Dimension.values()) {
            sumMap.put(d, 0);
            countMap.put(d, 0);
        }

        Map<String, Long> careerDistribution = new LinkedHashMap<>();

        for (AssessmentResult r : results) {
            if (r.getTraitScores() != null) {
                for (TraitScore ts : r.getTraitScores()) {
                    if (ts.getDimension() != null) {
                        sumMap.put(ts.getDimension(), sumMap.get(ts.getDimension()) + ts.getNormalizedScore());
                        countMap.put(ts.getDimension(), countMap.get(ts.getDimension()) + 1);
                    }
                }
            }

            if (r.getRecommendations() != null && !r.getRecommendations().isEmpty()) {
                String topCareer = r.getRecommendations().get(0).getCareerName();
                careerDistribution.put(topCareer, careerDistribution.getOrDefault(topCareer, 0L) + 1);
            }
        }

        Map<String, Double> dimensionAverages = new LinkedHashMap<>();
        for (Dimension d : Dimension.values()) {
            int count = countMap.get(d);
            double avg = count > 0 ? (double) sumMap.get(d) / count : 0.0;
            dimensionAverages.put(d.getDisplayName(), Math.round(avg * 10.0) / 10.0);
        }

        String mostCommonCareer = "N/A";
        int mostCommonCareerCount = 0;
        for (Map.Entry<String, Long> entry : careerDistribution.entrySet()) {
            if (entry.getValue() > mostCommonCareerCount) {
                mostCommonCareerCount = entry.getValue().intValue();
                mostCommonCareer = entry.getKey();
            }
        }

        return new CohortAnalyticsDto(
                total,
                mostCommonCareer,
                mostCommonCareerCount,
                dimensionAverages,
                careerDistribution
        );
    }
}
