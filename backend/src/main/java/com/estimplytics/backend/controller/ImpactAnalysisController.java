package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;
import com.estimplytics.backend.service.IImpactAnalysisService;
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
public class ImpactAnalysisController implements IImpactAnalysisController {

    private final IImpactAnalysisService service;

    public ImpactAnalysisController(IImpactAnalysisService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Page<ImpactAnalysisResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<ImpactAnalysisResponseDTO> getById(UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ImpactAnalysisResponseDTO> create(ImpactAnalysisRequestDTO request) {
        ImpactAnalysisResponseDTO response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ImpactAnalysisResponseDTO> update(UUID id, ImpactAnalysisUpdateDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<byte[]> exportDocx(UUID id) {
        return service.exportDocx(id)
            .map(export -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDisposition(ContentDisposition.attachment().filename(export.filename()).build());
                return new ResponseEntity<>(export.content(), headers, HttpStatus.OK);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
