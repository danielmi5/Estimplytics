package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationAlgorithmResultDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.ImpactAnalysis;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.ImpactAnalysisNotFoundException;
import com.estimplytics.backend.repository.ComponentAnalysisRepository;
import com.estimplytics.backend.repository.EstimationRepository;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstimationAlgorithmServiceTest {

    @Mock
    private ImpactAnalysisRepository impactAnalysisRepository;

    @Mock
    private ComponentAnalysisRepository componentAnalysisRepository;

    @Mock
    private EstimationRepository estimationRepository;

    @Mock
    private RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    @InjectMocks
    private EstimationAlgorithmService estimationAlgorithmService;

    @Test
    void calculateSuggestionForAnalysisId_shouldThrowException_whenAnalysisDoesNotExist() {
        UUID analysisId = UUID.randomUUID();

        when(impactAnalysisRepository.findById(analysisId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId))
            .isInstanceOf(ImpactAnalysisNotFoundException.class)
            .hasMessage("Impact analysis not found with id %s".formatted(analysisId));
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldReturnZeroValues_whenNoComponentsExist() {
        UUID analysisId = UUID.randomUUID();
        stubAnalysisLookup(analysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId)).thenReturn(List.of());

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(0);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(0);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldReturnZeroValues_whenNoHistoricalAnalysisExists() {
        UUID analysisId = UUID.randomUUID();
        UUID firstComponentId = UUID.randomUUID();
        UUID secondComponentId = UUID.randomUUID();
        stubAnalysisLookup(analysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId))
                .thenReturn(List.of(firstComponentId, secondComponentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(firstComponentId, secondComponentId), 2))
                .thenReturn(List.of(analysisId));

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService
                .calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(0);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(0);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldReturnZeroValues_whenHistoricalHoursAreEmpty() {
        UUID analysisId = UUID.randomUUID();
        UUID firstHistoricalAnalysisId = UUID.randomUUID();
        UUID secondHistoricalAnalysisId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        stubAnalysisLookup(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId)).thenReturn(List.of(componentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(componentId), 1))
            .thenReturn(List.of(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId));
        when(estimationRepository.findWithFeedbackByAnalysisIds(List.of(firstHistoricalAnalysisId, secondHistoricalAnalysisId)))
            .thenReturn(List.of());

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(0);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(0);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldReturnHighFiability_whenHistoricalDataIsConsistent() {
        UUID analysisId = UUID.randomUUID();
        UUID firstHistoricalAnalysisId = UUID.randomUUID();
        UUID secondHistoricalAnalysisId = UUID.randomUUID();
        UUID thirdHistoricalAnalysisId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        stubAnalysisLookup(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId))
                .thenReturn(List.of(componentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(componentId), 1))
                .thenReturn(List.of(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId));
        when(estimationRepository.findWithFeedbackByAnalysisIds(
                List.of(firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId)))
                .thenReturn(List.of(
                    buildEstimation(10, 2, 3, 4, 1),
                    buildEstimation(11, 2, 3, 5, 1),
                    buildEstimation(9, 2, 2, 4, 1)
                ));

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService
                .calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedHoursPlanning()).isEqualTo(2);
        assertThat(estimationAlgorithmResult.getSuggestedHoursAnalysis()).isEqualTo(3);
        assertThat(estimationAlgorithmResult.getSuggestedHoursDevelopment()).isEqualTo(4);
        assertThat(estimationAlgorithmResult.getSuggestedHoursTesting()).isEqualTo(1);
        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(10);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(80);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldUseTotalFeedbackAverage_whenPhaseHoursAreMissing() {
        UUID analysisId = UUID.randomUUID();
        UUID historicalAnalysisId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        stubAnalysisLookup(analysisId, historicalAnalysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId)).thenReturn(List.of(componentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(componentId), 1))
            .thenReturn(List.of(analysisId, historicalAnalysisId));
        when(estimationRepository.findWithFeedbackByAnalysisIds(List.of(historicalAnalysisId)))
            .thenReturn(List.of(buildEstimation(20, null, null, null, null)));

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedHoursPlanning()).isEqualTo(0);
        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(20);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(60);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldCapFiabilityAtMaximum_whenThereAreManyRecords() {
        UUID analysisId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UUID historicalId1 = UUID.randomUUID();
        UUID historicalId2 = UUID.randomUUID();
        UUID historicalId3 = UUID.randomUUID();
        UUID historicalId4 = UUID.randomUUID();
        UUID historicalId5 = UUID.randomUUID();
        UUID historicalId6 = UUID.randomUUID();
        stubAnalysisLookup(analysisId, historicalId1, historicalId2, historicalId3, historicalId4, historicalId5, historicalId6);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId)).thenReturn(List.of(componentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(componentId), 1))
            .thenReturn(List.of(analysisId, historicalId1, historicalId2, historicalId3, historicalId4, historicalId5, historicalId6));
        when(estimationRepository.findWithFeedbackByAnalysisIds(
            List.of(historicalId1, historicalId2, historicalId3, historicalId4, historicalId5, historicalId6)))
            .thenReturn(List.of(
                buildEstimation(10, 1, 2, 6, 1),
                buildEstimation(10, 1, 2, 6, 1),
                buildEstimation(10, 1, 2, 6, 1),
                buildEstimation(10, 1, 2, 6, 1),
                buildEstimation(10, 1, 2, 6, 1),
                buildEstimation(10, 1, 2, 6, 1)
            ));

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(10);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(95);
    }

    @Test
    void calculateSuggestionForAnalysisId_shouldApplyVolatilityPenalization_whenHistoricalDataIsVolatile() {
        UUID analysisId = UUID.randomUUID();
        UUID firstHistoricalAnalysisId = UUID.randomUUID();
        UUID secondHistoricalAnalysisId = UUID.randomUUID();
        UUID thirdHistoricalAnalysisId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        stubAnalysisLookup(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId);

        when(componentAnalysisRepository.findComponentIdsByAnalysisId(analysisId))
                .thenReturn(List.of(componentId));
        when(componentAnalysisRepository.findAnalysisIdsWithExactComponentSet(List.of(componentId), 1))
                .thenReturn(List.of(analysisId, firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId));
        when(estimationRepository.findWithFeedbackByAnalysisIds(
                List.of(firstHistoricalAnalysisId, secondHistoricalAnalysisId, thirdHistoricalAnalysisId)))
                .thenReturn(List.of(
                    buildEstimation(5, 1, 1, 2, 1),
                    buildEstimation(30, 5, 5, 15, 5),
                    buildEstimation(60, 10, 10, 30, 10)
                ));

        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService
                .calculateSuggestionForAnalysisId(analysisId);

        assertThat(estimationAlgorithmResult.getSuggestedTotalHours()).isEqualTo(31);
        assertThat(estimationAlgorithmResult.getFiabilityPercentage()).isEqualTo(65);
    }

    private Estimation buildEstimation(
        Integer actualHoursFeedback,
        Integer hoursPlanning,
        Integer hoursAnalysis,
        Integer hoursDevelopment,
        Integer hoursTesting
    ) {
        return Estimation.builder()
            .actualHoursFeedback(actualHoursFeedback)
            .hoursPlanning(hoursPlanning)
            .hoursAnalysis(hoursAnalysis)
            .hoursDevelopment(hoursDevelopment)
            .hoursTesting(hoursTesting)
            .build();
    }

    private void stubAnalysisLookup(UUID... analysisIds) {
        when(impactAnalysisRepository.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            for (UUID allowedId : analysisIds) {
                if (allowedId.equals(id)) {
                    return Optional.of(buildAnalysis(id));
                }
            }
            return Optional.empty();
        });
        when(redmineIssueMetadataRepository.findByRequestId(any())).thenReturn(Optional.empty());
    }

    private ImpactAnalysis buildAnalysis(UUID analysisId) {
        Request request = new Request();
        request.setId(UUID.randomUUID());

        ImpactAnalysis analysis = new ImpactAnalysis();
        analysis.setId(analysisId);
        analysis.setRequest(request);
        return analysis;
    }
}
