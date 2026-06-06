package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("/api/impact-analyses")
public interface IImpactAnalysisController extends ICrudController<ImpactAnalysisRequestDTO, ImpactAnalysisResponseDTO, ImpactAnalysisUpdateDTO, UUID> {

    @GetMapping("/request/{requestId}")
    ResponseEntity<ImpactAnalysisResponseDTO> getByRequestId(@PathVariable UUID requestId);

    @GetMapping("/{id}/export/docx")
    ResponseEntity<byte[]> exportDocx(@PathVariable UUID id);
}
