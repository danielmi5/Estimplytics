package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationUpdateDTO {
    @NotNull
    @Schema(description = "Version number", example = "2")
    private Integer versionNumber;

    @Schema(description = "Confidence percentage", example = "90")
    private Integer fiability;
    @Schema(description = "Estimated hours for planning", example = "4")
    private Integer hoursPlanning;
    @Schema(description = "Estimated hours for analysis", example = "8")
    private Integer hoursAnalysis;
    @Schema(description = "Estimated hours for development", example = "18")
    private Integer hoursDevelopment;
    @Schema(description = "Estimated hours for testing", example = "6")
    private Integer hoursTesting;
    @Schema(description = "Total hours", example = "30")
    private Integer totalHours;
    @Schema(description = "Actual hours feedback", example = "29")
    private Integer actualHoursFeedback;
    @Schema(description = "Justification for the estimation", example = "Adjustment after technical review")
    private String justification;

    @NotNull
    @Schema(description = "Update date and time", example = "2026-05-08T09:00:00")
    private LocalDateTime updatedAt;
}