package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ProjectRequestDTO;
import com.estimplytics.backend.dto.ProjectResponseDTO;
import com.estimplytics.backend.dto.ProjectUpdateDTO;
import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.exception.ProjectNotFoundException;
import com.estimplytics.backend.mapper.ProjectMapper;
import com.estimplytics.backend.repository.ProjectRepository;
import com.estimplytics.backend.repository.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final RequestRepository requestRepository;
    private final ProjectMapper projectMapper;
    private final OwnershipService ownershipService;

    public ProjectService(ProjectRepository projectRepository, RequestRepository requestRepository, ProjectMapper projectMapper, OwnershipService ownershipService) {
        this.projectRepository = projectRepository;
        this.requestRepository = requestRepository;
        this.projectMapper = projectMapper;
        this.ownershipService = ownershipService;
    }

    @Override
    public Page<ProjectResponseDTO> findAll(Pageable pageable) {
        if (ownershipService.isAdmin()) {
            return projectRepository.findAll(pageable).map(projectMapper::toResponseDTO);
        }
        return projectRepository.findByOwnerOrOwnerIsNull(ownershipService.currentUser(), pageable).map(projectMapper::toResponseDTO);
    }

    @Override
    public Optional<ProjectResponseDTO> findById(UUID id) {
        return projectRepository.findById(id).map(projectMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public ProjectResponseDTO create(ProjectRequestDTO dto) {
        Project entity = projectMapper.toEntity(dto);
        if (!ownershipService.isAdmin()) entity.setOwner(ownershipService.currentUser());
        return projectMapper.toResponseDTO(projectRepository.save(entity));
    }

    @Override
    @Transactional
    public ProjectResponseDTO update(UUID id, ProjectUpdateDTO dto) {
        return projectRepository.findById(id).map(entity -> {
            ownershipService.requireProjectOwner(entity);
            projectMapper.updateEntityFromDTO(dto, entity);
            return projectMapper.toResponseDTO(projectRepository.save(entity));
        }).orElseThrow(() -> new ProjectNotFoundException("Project not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException("Project not found with id %s".formatted(id)));
        ownershipService.requireProjectOwner(project);
        if (requestRepository.existsByProjectId(id)) {
            throw new IllegalArgumentException("Cannot delete project with associated manual requests");
        }
        projectRepository.delete(project);
    }
}
