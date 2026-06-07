package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class ProjectResponseDTO {

    @Schema(description = "Project unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project name", example = "Internal backlog")
    private String name;

    @Schema(description = "Project description", example = "Custom requests managed inside Estimplytics")
    private String description;

    @Schema(description = "Project creation date and time", example = "2026-05-08T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Owner identifier for manual projects")
    private UUID ownerId;
}
