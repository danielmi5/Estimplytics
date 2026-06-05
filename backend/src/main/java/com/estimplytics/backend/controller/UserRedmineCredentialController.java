package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.redmine.RedmineCredentialDTO;
import com.estimplytics.backend.dto.redmine.UserRedmineCredentialDto;
import com.estimplytics.backend.entity.RedmineInstance;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.entity.UserRedmineCredential;
import com.estimplytics.backend.repository.RedmineInstanceRepository;
import com.estimplytics.backend.repository.UserRedmineCredentialRepository;
import com.estimplytics.backend.service.OwnershipService;
import com.estimplytics.backend.service.RedmineIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/redmine-credentials")
@RequiredArgsConstructor
@Tag(name = "Redmine Credentials", description = "User Redmine credential management")
public class UserRedmineCredentialController {

    private final UserRedmineCredentialRepository repository;
    private final RedmineInstanceRepository instanceRepository;
    private final OwnershipService ownershipService;
    private final RedmineIntegrationService redmineIntegrationService;

    @GetMapping
    @Operation(summary = "Get current user Redmine credential")
    public List<UserRedmineCredentialDto> getMyCredentials() {
        User user = ownershipService.currentUser();
        return repository.findByUser(user)
            .map(cred -> List.of(new UserRedmineCredentialDto(
                cred.getId(),
                cred.getRedmineInstance().getId(),
                cred.getRedmineInstance().getBaseUrl(),
                cred.getApiKey() != null ? "********" : "",
                cred.getLastSyncAt()
            )))
            .orElse(List.of());
    }

    @PostMapping
    @Operation(summary = "Add or update a Redmine credential")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credential saved successfully"),
        @ApiResponse(responseCode = "400", description = "Redmine URL is required")
    })
    public ResponseEntity<Void> saveCredential(@RequestBody RedmineCredentialDTO redmineCredentials) {
        if (redmineCredentials.redmineUrl() == null || redmineCredentials.redmineUrl().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        User user = ownershipService.currentUser();

        RedmineInstance tempInstance = new RedmineInstance();
        tempInstance.setBaseUrl(redmineCredentials.redmineUrl());

        UserRedmineCredential tempCredential = new UserRedmineCredential();
        tempCredential.setRedmineInstance(tempInstance);
        tempCredential.setApiKey(redmineCredentials.plainApiKey());

        if (!"connected".equals(redmineIntegrationService.testConnection(tempCredential))) {
            return ResponseEntity.badRequest().build();
        }

        RedmineInstance instance = instanceRepository.findByBaseUrl(redmineCredentials.redmineUrl())
            .orElseGet(() -> {
                RedmineInstance newInstance = new RedmineInstance();
                newInstance.setName(redmineCredentials.redmineUrl());
                newInstance.setBaseUrl(redmineCredentials.redmineUrl());
                return instanceRepository.save(newInstance);
            });

        UserRedmineCredential credential = repository.findByUser(user).orElseGet(() -> {
            UserRedmineCredential c = new UserRedmineCredential();
            c.setUser(user);
            return c;
        });
        credential.setRedmineInstance(instance);

        if (redmineCredentials.plainApiKey() != null && !redmineCredentials.plainApiKey().isBlank()) {
            credential.setApiKey(redmineCredentials.plainApiKey());
        }

        repository.save(credential);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Redmine credential")
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        UserRedmineCredential credential = repository.findById(id).orElse(null);
        if (credential == null) return ResponseEntity.notFound().build();

        ownershipService.requireUserOrAdmin(credential.getUser().getId());
        repository.delete(credential);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/test")
    @Operation(summary = "Test Redmine connection via backend")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "connected | unauthorized | unreachable"),
        @ApiResponse(responseCode = "404", description = "Credential not found")
    })
    public ResponseEntity<Map<String, String>> testConnection(@PathVariable Long id) {
        UserRedmineCredential credential = repository.findById(id).orElse(null);
        if (credential == null) return ResponseEntity.notFound().build();

        ownershipService.requireUserOrAdmin(credential.getUser().getId());
        return ResponseEntity.ok(Map.of("status", redmineIntegrationService.testConnection(credential)));
    }
}
