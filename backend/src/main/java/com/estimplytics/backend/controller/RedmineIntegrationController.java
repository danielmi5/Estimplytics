package com.estimplytics.backend.controller;

import com.estimplytics.backend.service.RedmineIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedmineIntegrationController implements IRedmineIntegrationController {

    private final RedmineIntegrationService redmineIntegrationService;

    @Override
    public ResponseEntity<String> syncIssues(Long credentialId, boolean fullSync) {
        int syncedCount = redmineIntegrationService.syncIssuesFromRedmine(credentialId, fullSync);
        return ResponseEntity.ok("Synchronization successful. %d records synchronized.".formatted(syncedCount));
    }
}
