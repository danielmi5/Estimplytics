package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponseDTO {
    @Schema(description = "Request unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Manual project identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Redmine request identifier", example = "12345")
    private Integer redmineId;
    @Schema(description = "Redmine request code", example = "REQ-001")
    private String originRequestCode;
    @Schema(description = "Project name", example = "Web Portal")
    private String projectName;
    @Schema(description = "Demand type", example = "FEATURE")
    private String demandType;
    @Schema(description = "Request title", example = "Implement social login")
    private String title;
    @Schema(description = "Detailed description", example = "Allow access via Google and GitHub")
    private String description;
    @Schema(description = "Request status", example = "OPEN")
    private String status;
    @Schema(description = "Request priority", example = "HIGH")
    private String priority;
    @Schema(description = "Assigned person's name", example = "Daniel")
    private String assigneeName;
    @Schema(description = "Author name", example = "Daniel")
    private String authorName;
    @Schema(description = "Planned start date", example = "2026-05-10")
    private LocalDate startDate;
    @Schema(description = "Planned end date", example = "2026-05-20")
    private LocalDate endDate;
    @Schema(description = "Creation date in Redmine system", example = "2026-05-08T08:00:00")
    private LocalDateTime redmineCreatedDate;
    @Schema(description = "Last update date in Redmine system", example = "2026-05-09T10:00:00")
    private LocalDateTime redmineUpdatedDate;
    @Schema(description = "Closure date in Redmine system", example = "2026-05-15T18:00:00")
    private LocalDateTime redmineClosedDate;
    @Schema(description = "Progress percentage (0-100)", example = "50")
    private Integer doneRatio;
    @Schema(description = "Estimated hours", example = "40.5")
    private Double estimatedHours;
    @Schema(description = "Logged/spent hours", example = "10.0")
    private Double spentHours;

    @Schema(description = "Creation date and time", example = "2026-05-08T08:00:00")
    private LocalDateTime createdDate;

    @Schema(description = "Manual request owner identifier")
    private UUID ownerId;

    @Schema(description = "Analyst holding the edit lock")
    private UUID analysedById;

    @Schema(description = "Name of the analyst holding the edit lock")
    private String analysedByName;

    @Schema(description = "Timestamp when the edit lock was taken or renewed")
    private LocalDateTime analysedAt;
}
