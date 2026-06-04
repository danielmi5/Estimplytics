package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.service.IRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
public class RequestController implements IRequestController {

    private final IRequestService requestService;

    public RequestController(IRequestService requestService) {
        this.requestService = requestService;
    }

    @Override
    public ResponseEntity<Page<RequestResponseDTO>> getAll(Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(requestService.findAll(pageable, search));
    }

    @Override
    public ResponseEntity<RequestResponseDTO> getById(UUID id) {
        return requestService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<RequestResponseDTO> create(RequestRequestDTO request) {
        RequestResponseDTO response = requestService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<RequestResponseDTO> update(UUID id, RequestUpdateDTO request) {
        RequestResponseDTO response = requestService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        requestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RequestResponseDTO> lockForAnalysis(UUID id) {
        return ResponseEntity.ok(requestService.lockForAnalysis(id));
    }

    @Override
    public ResponseEntity<RequestResponseDTO> unlockFromAnalysis(UUID id) {
        return ResponseEntity.ok(requestService.unlockFromAnalysis(id));
    }
}
