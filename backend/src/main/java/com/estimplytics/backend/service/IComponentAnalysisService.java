package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface IComponentAnalysisService extends ICrudService<ComponentAnalysisRequestDTO, ComponentAnalysisResponseDTO, ComponentAnalysisUpdateDTO, UUID> {
    List<ComponentAnalysisResponseDTO> findByAnalysisId(UUID analysisId);
}