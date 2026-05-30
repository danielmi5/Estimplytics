package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/requests")
public interface IRequestController {

    @GetMapping
    @Operation(summary = "Get all records", description = "Returns a page with all available records")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Unauthorised")
    })
    ResponseEntity<Page<RequestResponseDTO>> getAll(Pageable pageable, @RequestParam(required = false) String search);

    @GetMapping("/{id}")
    @Operation(summary = "Get a record by ID")
    ResponseEntity<RequestResponseDTO> getById(@PathVariable UUID id);

    @PostMapping
    @Operation(summary = "Create a new record")
    ResponseEntity<RequestResponseDTO> create(@RequestBody RequestRequestDTO request);

    @PutMapping("/{id}")
    @Operation(summary = "Update a record")
    ResponseEntity<RequestResponseDTO> update(@PathVariable UUID id, @RequestBody RequestUpdateDTO updateRequest);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a record")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
