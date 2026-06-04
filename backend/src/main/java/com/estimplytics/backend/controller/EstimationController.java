package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import com.estimplytics.backend.service.IEstimationService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
public class EstimationController implements IEstimationController {

    private final IEstimationService service;

    public EstimationController(IEstimationService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Page<EstimationResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<EstimationResponseDTO> getById(UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<EstimationResponseDTO> create(EstimationRequestDTO request) {
        EstimationResponseDTO response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EstimationResponseDTO> update(UUID id, EstimationUpdateDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<byte[]> exportExcel(UUID id) {
        return service.exportExcel(id)
            .map(export -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDisposition(ContentDisposition.attachment().filename(export.filename()).build());
                return new ResponseEntity<>(export.content(), headers, HttpStatus.OK);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}