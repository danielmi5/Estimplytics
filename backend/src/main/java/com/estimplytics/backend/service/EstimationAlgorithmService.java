package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationAlgorithmResultDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.ImpactAnalysis;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.ImpactAnalysisNotFoundException;
import com.estimplytics.backend.repository.ComponentAnalysisRepository;
import com.estimplytics.backend.repository.EstimationRepository;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EstimationAlgorithmService {

    private static final Integer BASE_FIABILITY_PERCENTAGE = 50;
    private static final Integer FIABILITY_INCREMENT_PER_RECORD = 10;
    private static final Integer MAXIMUM_FIABILITY_PERCENTAGE = 95;
    private static final Integer HIGH_VOLATILITY_PENALIZATION = 15;
    private static final Double VOLATILITY_RANGE = 0.30;

    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final ComponentAnalysisRepository componentAnalysisRepository;
    private final EstimationRepository estimationRepository;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    public EstimationAlgorithmService(
            ImpactAnalysisRepository impactAnalysisRepository,
            ComponentAnalysisRepository componentAnalysisRepository,
            EstimationRepository estimationRepository,
            RedmineIssueMetadataRepository redmineIssueMetadataRepository
    ) {
        this.impactAnalysisRepository = impactAnalysisRepository;
        this.componentAnalysisRepository = componentAnalysisRepository;
        this.estimationRepository = estimationRepository;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
    }

    @Transactional(readOnly = true)
    public EstimationAlgorithmResultDTO calculateSuggestionForAnalysisId(UUID analysisId) {
        ImpactAnalysis targetAnalysis = impactAnalysisRepository.findById(analysisId).orElseThrow(() -> new ImpactAnalysisNotFoundException("Impact analysis not found with id %s".formatted(analysisId)));

        Request targetRequest = targetAnalysis.getRequest();
        RedmineIssueMetadata targetMetadata = (targetRequest == null) ? null: redmineIssueMetadataRepository.findByRequestId(targetRequest.getId()).orElse(null);

        List<UUID> componentIds = componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId);
        if (componentIds.isEmpty()) {
            return emptyResult();
        }

        List<UUID> historicalAnalysisIds = componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(componentIds,componentIds.size())
            .stream()
            .filter(candidateAnalysisId -> !candidateAnalysisId.equals(analysisId))
            .filter(candidateAnalysisId -> isValidIsolatedContext(targetMetadata, candidateAnalysisId))
            .toList();

        if (historicalAnalysisIds.isEmpty()) {
            return emptyResult();
        }

        List<Estimation> historicalEstimations = estimationRepository.findWithFeedbackByAnalysisIds(historicalAnalysisIds);

        if (historicalEstimations.isEmpty()) {
            return emptyResult();
        }

        List<Integer> historicalTotalHours = historicalEstimations.stream()
            .map(Estimation::getActualHoursFeedback)
            .toList();

        Integer suggestedPlanning = averagePhaseHours(
            historicalEstimations.stream().map(Estimation::getHoursPlanning).toList()
        );
        Integer suggestedAnalysis = averagePhaseHours(
            historicalEstimations.stream().map(Estimation::getHoursAnalysis).toList()
        );
        Integer suggestedDevelopment = averagePhaseHours(
            historicalEstimations.stream().map(Estimation::getHoursDevelopment).toList()
        );
        Integer suggestedTesting = averagePhaseHours(
            historicalEstimations.stream().map(Estimation::getHoursTesting).toList()
        );
        Integer suggestedTotalHours = suggestedPlanning + suggestedAnalysis + suggestedDevelopment + suggestedTesting;

        if (suggestedTotalHours == 0) {
            suggestedTotalHours = calculateAverageHours(historicalTotalHours);
        }

        return EstimationAlgorithmResultDTO.builder()
            .suggestedHoursPlanning(suggestedPlanning)
            .suggestedHoursAnalysis(suggestedAnalysis)
            .suggestedHoursDevelopment(suggestedDevelopment)
            .suggestedHoursTesting(suggestedTesting)
            .suggestedTotalHours(suggestedTotalHours)
            .fiabilityPercentage(calculateFiability(historicalTotalHours))
            .build();
    }

    private EstimationAlgorithmResultDTO emptyResult() {
        return EstimationAlgorithmResultDTO.builder()
            .suggestedHoursPlanning(0)
            .suggestedHoursAnalysis(0)
            .suggestedHoursDevelopment(0)
            .suggestedHoursTesting(0)
            .suggestedTotalHours(0)
            .fiabilityPercentage(0)
            .build();
    }

    private Integer averagePhaseHours(List<Integer> phaseHours) {
        List<Integer> validHours = phaseHours.stream()
            .filter(hours -> hours != null && hours > 0)
            .toList();

        return calculateAverageHours(validHours);
    }

    private boolean isValidIsolatedContext(RedmineIssueMetadata targetMetadata, UUID historicalAnalysisId) {
        ImpactAnalysis hAnalysis = impactAnalysisRepository.findById(historicalAnalysisId).orElse(null);
        if (hAnalysis == null || hAnalysis.getRequest() == null) return false;
        
        RedmineIssueMetadata historicalMetadata = redmineIssueMetadataRepository.findByRequestId(hAnalysis.getRequest().getId()).orElse(null);
        
        if (targetMetadata == null && historicalMetadata == null) return true;

        if (targetMetadata == null || historicalMetadata == null) return false;

        if (targetMetadata.getRedmineInstance() == null || historicalMetadata.getRedmineInstance() == null) return false;
        if (targetMetadata.getProjectId() == null || historicalMetadata.getProjectId() == null) return false;
        
        return targetMetadata.getRedmineInstance().getId().equals(historicalMetadata.getRedmineInstance().getId()) &&
               targetMetadata.getProjectId().equals(historicalMetadata.getProjectId());
    }

    private Integer calculateAverageHours(List<Integer> historicalHours) {
        Double average = historicalHours.stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);

        if (average <= 0.0) {
            return 0;
        }
        return (int) Math.round(average);
    }

    private Integer calculateFiability(List<Integer> historicalHours) {
        Integer records = historicalHours.size();
        Integer fiabilityBase = Math.min(BASE_FIABILITY_PERCENTAGE + records * FIABILITY_INCREMENT_PER_RECORD, MAXIMUM_FIABILITY_PERCENTAGE );

        Double average = historicalHours.stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);

        if (average <= 0.0) {
            return fiabilityBase;
        }

        Double standardDeviation = calculateStandardDeviation(historicalHours, average);
        Double volatility = standardDeviation / average;

        if (volatility > VOLATILITY_RANGE) {
            return Math.max(fiabilityBase - HIGH_VOLATILITY_PENALIZATION, 0);
        }

        return fiabilityBase;
    }

    private Double calculateStandardDeviation(List<Integer> historicalHours, Double average) {
        Double variance = historicalHours.stream()
            .mapToDouble(hours -> Math.pow(hours - average, 2))
            .average()
            .orElse(0.0);

        if (variance <= 0.0) {
            return 0.0;
        }
        return Math.sqrt(variance);
    }
}
