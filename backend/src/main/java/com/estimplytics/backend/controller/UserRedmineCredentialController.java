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
import org.springframework.security.core.Authentication;
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
    @Operation(summary = "List current user Redmine credentials")
    public List<UserRedmineCredentialDto> getMyCredentials(Authentication auth) {
        User user = ownershipService.resolveCurrentUser(auth);
        return repository.findByUser(user).stream()
            .map(cred -> new UserRedmineCredentialDto(
                cred.getId(),
                cred.getRedmineInstance().getId(),
                cred.getRedmineInstance().getBaseUrl(),
                cred.getApiKey() != null ? "********" : "",
                cred.getLastSyncAt()
            ))
            .toList();
    }

    @PostMapping
    @Operation(summary = "Add or update a Redmine credential")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credential saved successfully"),
        @ApiResponse(responseCode = "400", description = "Redmine URL is required")
    })
    public ResponseEntity<Void> saveCredential(Authentication auth, @RequestBody RedmineCredentialDTO redmineCredentials) {
        if (redmineCredentials.redmineUrl() == null || redmineCredentials.redmineUrl().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        User user = ownershipService.resolveCurrentUser(auth);

        RedmineInstance instance = instanceRepository.findByBaseUrl(redmineCredentials.redmineUrl())
            .orElseGet(() -> {
                RedmineInstance newInstance = new RedmineInstance();
                newInstance.setName(redmineCredentials.redmineUrl());
                newInstance.setBaseUrl(redmineCredentials.redmineUrl());
                return instanceRepository.save(newInstance);
            });

        repository.findByUser(user).stream()
            .filter(c -> !c.getRedmineInstance().getId().equals(instance.getId()))
            .forEach(repository::delete);

        UserRedmineCredential credential = repository.findByUserAndRedmineInstance(user, instance)
            .orElseGet(() -> {
                UserRedmineCredential c = new UserRedmineCredential();
                c.setUser(user);
                c.setRedmineInstance(instance);
                return c;
            });

        if (redmineCredentials.plainApiKey() != null && !redmineCredentials.plainApiKey().isBlank()) {
            credential.setApiKey(redmineCredentials.plainApiKey());
        }

        repository.save(credential);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Redmine credential")
    public ResponseEntity<Void> deleteCredential(Authentication auth, @PathVariable Long id) {
        UserRedmineCredential credential = repository.findById(id).orElse(null);
        if (credential == null) return ResponseEntity.notFound().build();

        ownershipService.requireSelfOrAdmin(credential.getUser().getId(), auth);
        repository.delete(credential);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/test")
    @Operation(summary = "Test Redmine connection via backend")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "connected | unauthorized | unreachable"),
        @ApiResponse(responseCode = "404", description = "Credential not found")
    })
    public ResponseEntity<Map<String, String>> testConnection(Authentication auth, @PathVariable Long id) {
        UserRedmineCredential credential = repository.findById(id).orElse(null);
        if (credential == null) return ResponseEntity.notFound().build();

        ownershipService.requireSelfOrAdmin(credential.getUser().getId(), auth);
        return ResponseEntity.ok(Map.of("status", redmineIntegrationService.testConnection(credential)));
    }
}
