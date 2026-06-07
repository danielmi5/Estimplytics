package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.redmine.RedmineIssueDTO;
import com.estimplytics.backend.dto.redmine.RedmineIssueResponseDTO;
import com.estimplytics.backend.entity.UserRedmineCredential;
import com.estimplytics.backend.exception.RedmineCredentialNotFoundException;
import com.estimplytics.backend.exception.RedmineIntegrationException;
import com.estimplytics.backend.exception.RedmineIntegrationException.ErrorType;
import com.estimplytics.backend.repository.UserRedmineCredentialRepository;
import com.estimplytics.backend.util.RedmineDateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
public class RedmineIntegrationService {

    private final RestClient restClient;
    private final UserRedmineCredentialRepository userRedmineCredentialRepository;
    private final RedmineIssuePersistenceService redmineIssuePersistenceService;
    private final OwnershipService ownershipService;

    public RedmineIntegrationService(RestClient.Builder restClientBuilder, UserRedmineCredentialRepository userRedmineCredentialRepository, RedmineIssuePersistenceService redmineIssuePersistenceService, OwnershipService ownershipService) {
        this.restClient = restClientBuilder.build();
        this.userRedmineCredentialRepository = userRedmineCredentialRepository;
        this.redmineIssuePersistenceService = redmineIssuePersistenceService;
        this.ownershipService = ownershipService;
    }

    public String resolveBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return baseUrl;
        }
        String url = baseUrl.trim().replaceAll("/+$", "");
        if (url.equalsIgnoreCase("http://redmine.org") || url.equalsIgnoreCase("https://redmine.org")) {
            return "https://www.redmine.org";
        }
        return url;
    }

    public String testConnection(UserRedmineCredential credential) {
        String base = resolveBaseUrl(credential.getRedmineInstance().getBaseUrl());
        try {
            applyApiKey(restClient.get().uri(base + "/issues.json?limit=1"), credential.getApiKey())
                    .retrieve()
                    .body(RedmineIssueResponseDTO.class);
            return "connected";
        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            return (code == 401 || code == 403) ? "unauthorized" : "unreachable";
        } catch (Exception e) {
            return "unreachable";
        }
    }

    public int syncIssuesFromRedmine(Long credentialId, boolean fullSync) {
        UserRedmineCredential credential = userRedmineCredentialRepository.findById(credentialId).orElseThrow(() -> new RedmineCredentialNotFoundException("Redmine credential not found with id %s".formatted(credentialId)));
        ownershipService.requireUserOrAdmin(credential.getUser().getId());

        if (fullSync) {
            credential.setLastSyncAt(null);
        }

        int offset = 0;
        int limit = 100;
        int totalSynced = 0;
        Integer totalCount = null;

        try {
            String baseUrl = resolveBaseUrl(credential.getRedmineInstance().getBaseUrl());

            do {
                String apiUrl = "%s/issues.json?status_id=*&limit=%d&offset=%d".formatted(baseUrl, limit, offset);

                if (!fullSync && credential.getLastSyncAt() != null) {
                    apiUrl += "&updated_on=>=" + RedmineDateFormatter.formatUpdatedOnFilter(credential.getLastSyncAt());
                }

                RedmineIssueResponseDTO response = applyApiKey(restClient.get().uri(apiUrl), credential.getApiKey()).retrieve().body(RedmineIssueResponseDTO.class);

                if (response == null || response.getIssues() == null || response.getIssues().isEmpty()) {
                    break;
                }

                if (totalCount == null) {
                    totalCount = response.getTotalCount();
                }

                for (RedmineIssueDTO issueDto : response.getIssues()) {
                    try {
                        redmineIssuePersistenceService.upsertIssue(issueDto, credential);
                        totalSynced++;
                    } catch (Exception e) {
                        System.err.println("Error sincronizando ticket " + issueDto.getId() + ": " + e.getMessage());
                    }
                }

                offset += limit;

            } while (totalCount != null && offset < totalCount);

            credential.setLastSyncAt(LocalDateTime.now());
            userRedmineCredentialRepository.save(credential);

            return totalSynced;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RedmineIntegrationException("Invalid Redmine API credentials or key", e, ErrorType.INVALID_CREDENTIALS);
            }
            throw new RedmineIntegrationException("HTTP client error: %s".formatted(e.getStatusCode()), e, ErrorType.NETWORK_ERROR);
        } catch (ResourceAccessException e) {
            throw new RedmineIntegrationException("Network error: unable to reach Redmine server", e, ErrorType.NETWORK_ERROR);
        } catch (HttpServerErrorException e) {
            throw new RedmineIntegrationException("Redmine server error: %s".formatted(e.getStatusCode()), e, ErrorType.NETWORK_ERROR);
        } catch (RedmineCredentialNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RedmineIntegrationException("Failed to process Redmine response: %s".formatted(e.getMessage()), e, ErrorType.INVALID_PAYLOAD);
        }
    }

    private RestClient.RequestHeadersSpec<?> applyApiKey(RestClient.RequestHeadersSpec<?> spec, String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            return spec.header("X-Redmine-API-Key", apiKey);
        }
        return spec;
    }
}
