package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("/api/estimations")
public interface IEstimationController extends ICrudController<EstimationRequestDTO, EstimationResponseDTO, EstimationUpdateDTO, UUID> {

    @GetMapping("/{id}/export/excel")
    ResponseEntity<byte[]> exportExcel(@PathVariable UUID id);
}