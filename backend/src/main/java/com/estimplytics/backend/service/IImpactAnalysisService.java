package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;

import java.util.Optional;
import java.util.UUID;

public interface IImpactAnalysisService extends ICrudService<ImpactAnalysisRequestDTO, ImpactAnalysisResponseDTO, ImpactAnalysisUpdateDTO, UUID> {

    record DocxExport(byte[] content, String filename) {}

    Optional<DocxExport> exportDocx(UUID id);
}
