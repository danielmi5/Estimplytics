package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationHistoryUpdateDTO {
    @NotNull
    @Schema(description = "Frozen version", example = "2")
    private Integer frozenVersion;

    @NotNull
    @Schema(description = "Historical estimation data", example = "{\"hoursPlanning\":4,\"hoursAnalysis\":8,\"hoursDevelopment\":18,\"hoursTesting\":6}")
    private Map<String, Object> snapshotData;

    @NotNull
    @Schema(description = "Modification date and time", example = "2026-05-08T09:00:00")
    private LocalDateTime modifiedAt;
}
