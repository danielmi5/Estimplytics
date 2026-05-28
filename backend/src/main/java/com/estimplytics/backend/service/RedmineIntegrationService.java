package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.redmine.RedmineIssueDTO;
import com.estimplytics.backend.dto.redmine.RedmineIssueResponseDTO;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.UserRedmineCredential;
import com.estimplytics.backend.exception.RedmineCredentialNotFoundException;
import com.estimplytics.backend.exception.RedmineIntegrationException;
import com.estimplytics.backend.exception.RedmineIntegrationException.ErrorType;
import com.estimplytics.backend.mapper.RedmineIssueMapper;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
import com.estimplytics.backend.repository.UserRedmineCredentialRepository;
import com.estimplytics.backend.util.RedmineDateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RedmineIntegrationService {

    private final RestClient restClient;
    private final RequestRepository requestRepository;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;
    private final UserRedmineCredentialRepository userRedmineCredentialRepository;
    private final RedmineIssueMapper redmineIssueMapper;

    public RedmineIntegrationService(RestClient.Builder restClientBuilder,
                                     RequestRepository requestRepository,
                                     RedmineIssueMetadataRepository redmineIssueMetadataRepository,
                                     UserRedmineCredentialRepository userRedmineCredentialRepository,
                                     RedmineIssueMapper redmineIssueMapper) {
        this.restClient = restClientBuilder.build();
        this.requestRepository = requestRepository;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
        this.userRedmineCredentialRepository = userRedmineCredentialRepository;
        this.redmineIssueMapper = redmineIssueMapper;
    }

    public String testConnection(UserRedmineCredential credential) {
        String base = credential.getRedmineInstance().getBaseUrl().replaceAll("/+$", "");
        try {
            var spec = restClient.get().uri(base);
            String key = credential.getApiKey();

            if (key != null && !key.isBlank()) spec = spec.header("X-Redmine-API-Key", key);
            
            spec.retrieve().toBodilessEntity();
            return "connected";
        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            return (code == 401 || code == 403) ? "unauthorized" : "unreachable";
        } catch (Exception e) {
            return "unreachable";
        }
    }

    @Transactional
    public int syncIssuesFromRedmine(Long credentialId) {
        UserRedmineCredential credential = userRedmineCredentialRepository.findById(credentialId).orElseThrow(() -> new RedmineCredentialNotFoundException("Redmine credential not found with id %s".formatted(credentialId)));

        int offset = 0;
        int limit = 100;
        int totalSynced = 0;
        Integer totalCount = null;

        try {
            do {
                String apiUrl = "%s/issues.json?status_id=*&limit=%d&offset=%d".formatted(
                        credential.getRedmineInstance().getBaseUrl(), limit, offset);

                if (credential.getLastSyncAt() != null) {
                    apiUrl += "&updated_on=>=" + RedmineDateFormatter.formatUpdatedOnFilter(credential.getLastSyncAt());
                }

                RedmineIssueResponseDTO response = restClient.get()
                        .uri(apiUrl)
                        .header("X-Redmine-API-Key", credential.getApiKey())
                        .retrieve()
                        .body(RedmineIssueResponseDTO.class);

                if (response == null || response.getIssues() == null || response.getIssues().isEmpty()) {
                    break;
                }

                if (totalCount == null) {
                    totalCount = response.getTotalCount();
                }

                for (RedmineIssueDTO issueDto : response.getIssues()) {
                    upsertIssue(issueDto, credential);
                    totalSynced++;
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

    private void upsertIssue(RedmineIssueDTO dto, UserRedmineCredential credential) {
        Optional<RedmineIssueMetadata> existingMetadata = redmineIssueMetadataRepository
                .findByRedmineIdAndRedmineInstanceId(dto.getId(), credential.getRedmineInstance().getId());

        Request request;
        RedmineIssueMetadata metadata;

        if (existingMetadata.isPresent()) {
            metadata = existingMetadata.get();
            request = metadata.getRequest();
        } else {
            request = new Request();
            metadata = new RedmineIssueMetadata();
            metadata.setRedmineInstance(credential.getRedmineInstance());
        }

        redmineIssueMapper.updateEntityAndMetadataFromDto(dto, request, metadata);

        request = requestRepository.save(request);
        metadata.setRequest(request);
        redmineIssueMetadataRepository.save(metadata);
    }
}
