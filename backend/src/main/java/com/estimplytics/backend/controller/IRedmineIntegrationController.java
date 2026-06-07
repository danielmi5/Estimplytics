package com.estimplytics.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/redmine")
public interface IRedmineIntegrationController {
    @Operation(summary = "Synchronize issues from Redmine", description = "Fetches issues from Redmine using the provided credential and upserts them as Requests in the local database. Requires ADMIN authority.")
    @ApiResponse(responseCode = "200", description = "Successful synchronization")
    @ApiResponse(responseCode = "404", description = "Redmine credential not found")
    @ApiResponse(responseCode = "502", description = "Error communicating with Redmine or processing data")
    @PostMapping("/sync")
    ResponseEntity<String> syncIssues(@RequestParam("credentialId") Long credentialId, @RequestParam(value = "fullSync", defaultValue = "false") boolean fullSync);
}
