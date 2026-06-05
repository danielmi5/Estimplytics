package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestRequestDTO {

    @Schema(description = "Manual project identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Project name for manual create (reuses or creates for the current user)", example = "Portal Clientes")
    private String projectName;

    @Schema(description = "Demand type", example = "FEATURE")
    private String demandType;

    @NotBlank
    @Schema(description = "Request title", example = "Implement social login")
    private String title;

    @Schema(description = "Detailed description", example = "Allow access via Google and GitHub")
    private String description;

    @NotBlank
    @Schema(description = "Request status", example = "OPEN")
    private String status;

    @NotBlank
    @Schema(description = "Request priority", example = "HIGH")
    private String priority;

    @Schema(description = "Planned start date", example = "2026-05-10")
    private LocalDate startDate;

    @Schema(description = "Planned end date", example = "2026-05-20")
    private LocalDate endDate;

    @Schema(description = "Progress percentage (0-100)", example = "50")
    private Integer doneRatio;

    @Schema(description = "Estimated hours", example = "40.5")
    private Double estimatedHours;

    @Schema(description = "Logged/spent hours", example = "10.0")
    private Double spentHours;
}
