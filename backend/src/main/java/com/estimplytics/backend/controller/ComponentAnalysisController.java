package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;
import com.estimplytics.backend.service.IComponentAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
public class ComponentAnalysisController implements IComponentAnalysisController {

    private final IComponentAnalysisService service;

    public ComponentAnalysisController(IComponentAnalysisService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Page<ComponentAnalysisResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<ComponentAnalysisResponseDTO> getById(UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ComponentAnalysisResponseDTO> create(ComponentAnalysisRequestDTO request) {
        ComponentAnalysisResponseDTO response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ComponentAnalysisResponseDTO> update(UUID id, ComponentAnalysisUpdateDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Override
    public ResponseEntity<List<ComponentAnalysisResponseDTO>> getByAnalysisId(UUID analysisId) {
        return ResponseEntity.ok(service.findByAnalysisId(analysisId));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}