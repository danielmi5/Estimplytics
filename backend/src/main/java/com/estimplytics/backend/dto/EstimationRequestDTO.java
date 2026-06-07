package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationRequestDTO {
    @NotNull
    @Schema(description = "Associated analysis identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID analysisId;

    @NotNull
    @Schema(description = "Version number", example = "1")
    private Integer versionNumber;

    @Schema(description = "Confidence percentage", example = "85")
    private Integer fiability;
    @Schema(description = "Estimated hours for planning", example = "5")
    private Integer hoursPlanning;
    @Schema(description = "Estimated hours for analysis", example = "10")
    private Integer hoursAnalysis;
    @Schema(description = "Estimated hours for development", example = "20")
    private Integer hoursDevelopment;
    @Schema(description = "Estimated hours for testing", example = "8")
    private Integer hoursTesting;
    @Schema(description = "Total hours", example = "35")
    private Integer totalHours;
    @Schema(description = "Actual hours feedback", example = "32")
    private Integer actualHoursFeedback;
    @Schema(description = "Justification for the estimation", example = "Existing infrastructure reuse")
    private String justification;

    @NotNull
    @Schema(description = "Update date and time", example = "2026-05-08T08:00:00")
    private LocalDateTime updatedAt;
}