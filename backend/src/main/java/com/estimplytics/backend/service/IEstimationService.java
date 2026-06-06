package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;

import java.util.Optional;
import java.util.UUID;

public interface IEstimationService extends ICrudService<EstimationRequestDTO, EstimationResponseDTO, EstimationUpdateDTO, UUID> {

    record ExcelExport(byte[] content, String filename) {}

    Optional<ExcelExport> exportExcel(UUID id);

    Optional<EstimationResponseDTO> findByAnalysisId(UUID analysisId);
}