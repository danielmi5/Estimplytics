package com.estimplytics.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

public interface ICrudController<Request, Response, Update, ID> {
    
    @GetMapping
    @Operation(summary = "Get all records", description = "Returns a page with all available records")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Records retrieved successfully", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Unauthorised")
    })
    ResponseEntity<Page<Response>> getAll(Pageable pageable);
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a record by ID", description = "Returns a specific record by its identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record found", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Record not found"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    ResponseEntity<Response> getById(@PathVariable ID id);
    
    @PostMapping
    @Operation(summary = "Create a new record", description = "Creates a new record with the provided data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Record created successfully", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    ResponseEntity<Response> create(@Valid @RequestBody Request request);
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a record", description = "Updates an existing record with new data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record updated successfully", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Record not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    ResponseEntity<Response> update(@PathVariable ID id, @Valid @RequestBody Update updateRequest);
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a record", description = "Deletes a specific record by its identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    ResponseEntity<Void> delete(@PathVariable ID id);
}
