package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/component-analyses")
public interface IComponentAnalysisController extends ICrudController<ComponentAnalysisRequestDTO, ComponentAnalysisResponseDTO, ComponentAnalysisUpdateDTO, UUID> {
    @GetMapping("/analysis/{analysisId}")
    ResponseEntity<List<ComponentAnalysisResponseDTO>> getByAnalysisId(@PathVariable UUID analysisId);
}