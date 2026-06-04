package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.exception.RequestNotFoundException;
import com.estimplytics.backend.mapper.RequestMapper;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RequestService implements IRequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final RedmineIssueMetadataRepository redmineMetadataRepository;
    private final OwnershipService ownershipService;

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper, RedmineIssueMetadataRepository redmineMetadataRepository, OwnershipService ownershipService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.redmineMetadataRepository = redmineMetadataRepository;
        this.ownershipService = ownershipService;
    }

    private void rejectIfRedmineSourced(UUID id) {
        if (redmineMetadataRepository.findByRequestId(id).isPresent()) {
            throw new AccessDeniedException("Redmine requests cannot be modified or deleted");
        }
    }

    private Page<Request> findAccessible(Pageable pageable, String search) {
        User current = ownershipService.currentUser();
        Optional<Long> instanceId = ownershipService.redmineInstanceId(current);
        boolean hasSearch = search != null && !search.isBlank();
        if (instanceId.isEmpty()) {
            return hasSearch ? requestRepository.searchManualAccessible(current.getId(), search.trim(), pageable) : requestRepository.findManualAccessible(current.getId(), pageable);
        }
        return hasSearch ? requestRepository.searchAllAccessible(current.getId(), instanceId.get(), search.trim(), pageable) : requestRepository.findAllAccessible(current.getId(), instanceId.get(), pageable);
    }

    @Override
    public Page<RequestResponseDTO> findAll(Pageable pageable) {
        return findAll(pageable, null);
    }

    @Override
    public Page<RequestResponseDTO> findAll(Pageable pageable, String search) {
        return requestMapper.toResponseDTOPage(findAccessible(pageable, search));
    }

    @Override
    public Optional<RequestResponseDTO> findById(UUID id) {
        return requestRepository.findById(id)
                .filter(ownershipService::canView)
                .map(requestMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public RequestResponseDTO create(RequestRequestDTO dto) {
        Request entity = requestMapper.toEntity(dto);
        ownershipService.requireOwnedProject(entity.getProject());
        if (!ownershipService.isAdmin()) entity.setOwner(ownershipService.currentUser());
        return requestMapper.toResponseDTO(requestRepository.save(entity));
    }

    @Override
    @Transactional
    public RequestResponseDTO update(UUID id, RequestUpdateDTO dto) {
        rejectIfRedmineSourced(id);
        Request request = requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException("Request not found with id %s".formatted(id)));
        ownershipService.requireRequestOwner(request);
        requestMapper.updateEntityFromDTO(dto, request);
        return requestMapper.toResponseDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        rejectIfRedmineSourced(id);
        Request request = requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException("Request not found with id %s".formatted(id)));
        ownershipService.requireRequestOwner(request);
        requestRepository.delete(request);
    }

    @Override
    @Transactional
    public RequestResponseDTO lockForAnalysis(UUID requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RequestNotFoundException("Request not found with id %s".formatted(requestId)));
        ownershipService.lock(request);
        return requestMapper.toResponseDTO(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestResponseDTO unlockFromAnalysis(UUID requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RequestNotFoundException("Request not found with id %s".formatted(requestId)));
        ownershipService.unlock(request);
        return requestMapper.toResponseDTO(requestRepository.save(request));
    }
}
