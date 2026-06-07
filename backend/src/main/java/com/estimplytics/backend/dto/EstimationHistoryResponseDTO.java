package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationHistoryResponseDTO {
    @Schema(description = "History identifier", example = "1")
    private Long id;
    @Schema(description = "Estimation identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID estimationId;
    @Schema(description = "Frozen version", example = "1")
    private Integer frozenVersion;
    @Schema(description = "Historical estimation data", example = "{\"hoursPlanning\":5,\"hoursAnalysis\":10,\"hoursDevelopment\":20,\"hoursTesting\":8}")
    private Map<String, Object> snapshotData;
    @Schema(description = "Modification date and time", example = "2026-05-08T08:30:00")
    private LocalDateTime modifiedAt;
}
