package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.exception.ProjectNotFoundException;
import com.estimplytics.backend.exception.RequestNotFoundException;
import com.estimplytics.backend.mapper.RequestMapper;
import com.estimplytics.backend.repository.ProjectRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
import com.estimplytics.backend.util.ManualRequestCodeFormatter;
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
    private final ProjectRepository projectRepository;

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper, RedmineIssueMetadataRepository redmineMetadataRepository, OwnershipService ownershipService, ProjectRepository projectRepository) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.redmineMetadataRepository = redmineMetadataRepository;
        this.ownershipService = ownershipService;
        this.projectRepository = projectRepository;
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
        Project project = resolveProject(dto);
        ownershipService.requireOwnedProject(project);
        Request entity = requestMapper.toEntity(dto, project);
        long sequence = requestRepository.countManualByProjectId(project.getId()) + 1;
        entity.setOriginRequestCode(ManualRequestCodeFormatter.format(project.getName(), sequence));
        if (!ownershipService.isAdmin()) entity.setOwner(ownershipService.currentUser());
        return requestMapper.toResponseDTO(requestRepository.save(entity));
    }

    private Project resolveProject(RequestRequestDTO dto) {
        if (dto.getProjectId() != null) {
            return projectRepository.findById(dto.getProjectId()).orElseThrow(() -> new ProjectNotFoundException("Project not found with id %s".formatted(dto.getProjectId())));
        }

        String name = dto.getProjectName() == null ? "" : dto.getProjectName().trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Project id or name is required");
        }

        User current = ownershipService.currentUser();
        return projectRepository.findByNameIgnoreCaseAndOwner_Id(name, current.getId())
                .orElseGet(() -> {
                    Project project = new Project();
                    project.setName(name);
                    project.setOwner(current);
                    return projectRepository.save(project);
                });
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
