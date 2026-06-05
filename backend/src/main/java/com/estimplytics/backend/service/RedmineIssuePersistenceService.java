package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.redmine.RedmineIssueDTO;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.UserRedmineCredential;
import com.estimplytics.backend.mapper.RedmineIssueMapper;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RedmineIssuePersistenceService {

    private final RequestRepository requestRepository;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;
    private final RedmineIssueMapper redmineIssueMapper;

    public RedmineIssuePersistenceService(RequestRepository requestRepository, RedmineIssueMetadataRepository redmineIssueMetadataRepository, RedmineIssueMapper redmineIssueMapper) {
        this.requestRepository = requestRepository;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
        this.redmineIssueMapper = redmineIssueMapper;
    }

    @Transactional
    public void upsertIssue(RedmineIssueDTO dto, UserRedmineCredential credential) {
        Optional<RedmineIssueMetadata> existingMetadata = redmineIssueMetadataRepository.findByRedmineIdAndRedmineInstanceId(dto.getId(), credential.getRedmineInstance().getId());

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
