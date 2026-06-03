package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationAlgorithmResultDTO {
    @Schema(description = "Suggested hours for planning", example = "5")
    private Integer suggestedHoursPlanning;
    @Schema(description = "Suggested hours for analysis", example = "10")
    private Integer suggestedHoursAnalysis;
    @Schema(description = "Suggested hours for development", example = "15")
    private Integer suggestedHoursDevelopment;
    @Schema(description = "Suggested hours for testing", example = "5")
    private Integer suggestedHoursTesting;
    @Schema(description = "Total hours suggested by the algorithm", example = "35")
    private Integer suggestedTotalHours;
    @Schema(description = "Suggested confidence percentage", example = "85")
    private Integer fiabilityPercentage;
}
