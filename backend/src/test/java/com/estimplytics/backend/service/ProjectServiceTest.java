package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ProjectRequestDTO;
import com.estimplytics.backend.dto.ProjectResponseDTO;
import com.estimplytics.backend.dto.ProjectUpdateDTO;
import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.exception.ProjectNotFoundException;
import com.estimplytics.backend.mapper.ProjectMapper;
import com.estimplytics.backend.repository.ProjectRepository;
import com.estimplytics.backend.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private OwnershipService ownershipService;

    @InjectMocks
    private ProjectService service;

    @Test
    void findAll_shouldMapPageForAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Project entity = mock(Project.class);
        ProjectResponseDTO response = mock(ProjectResponseDTO.class);
        Page<Project> page = new PageImpl<>(List.of(entity));
        when(ownershipService.isAdmin()).thenReturn(true);
        when(projectRepository.findAll(pageable)).thenReturn(page);
        when(projectMapper.toResponseDTO(entity)).thenReturn(response);

        Page<ProjectResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findAll_shouldReturnOwnedProjectsForAnalyst() {
        Pageable pageable = PageRequest.of(0, 10);
        Project entity = mock(Project.class);
        ProjectResponseDTO response = mock(ProjectResponseDTO.class);
        User owner = mock(User.class);
        Page<Project> page = new PageImpl<>(List.of(entity));
        when(ownershipService.isAdmin()).thenReturn(false);
        when(ownershipService.currentUser()).thenReturn(owner);
        when(projectRepository.findByOwnerOrOwnerIsNull(owner, pageable)).thenReturn(page);
        when(projectMapper.toResponseDTO(entity)).thenReturn(response);

        Page<ProjectResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnMappedOptionalWhenExists() {
        UUID id = UUID.randomUUID();
        Project entity = mock(Project.class);
        ProjectResponseDTO response = mock(ProjectResponseDTO.class);
        when(projectRepository.findById(id)).thenReturn(Optional.of(entity));
        when(projectMapper.toResponseDTO(entity)).thenReturn(response);

        Optional<ProjectResponseDTO> result = service.findById(id);

        assertThat(result).contains(response);
    }

    @Test
    void create_shouldPersistAndMapEntity() {
        ProjectRequestDTO request = mock(ProjectRequestDTO.class);
        Project entity = mock(Project.class);
        Project saved = mock(Project.class);
        ProjectResponseDTO response = mock(ProjectResponseDTO.class);
        User owner = mock(User.class);
        when(projectMapper.toEntity(request)).thenReturn(entity);
        when(ownershipService.isAdmin()).thenReturn(false);
        when(ownershipService.currentUser()).thenReturn(owner);
        when(projectRepository.save(entity)).thenReturn(saved);
        when(projectMapper.toResponseDTO(saved)).thenReturn(response);

        ProjectResponseDTO result = service.create(request);

        assertThat(result).isSameAs(response);
        verify(entity).setOwner(owner);
    }

    @Test
    void update_shouldPersistAndMapWhenEntityExists() {
        UUID id = UUID.randomUUID();
        ProjectUpdateDTO update = mock(ProjectUpdateDTO.class);
        Project entity = mock(Project.class);
        Project saved = mock(Project.class);
        ProjectResponseDTO response = mock(ProjectResponseDTO.class);
        when(projectRepository.findById(id)).thenReturn(Optional.of(entity));
        when(projectRepository.save(entity)).thenReturn(saved);
        when(projectMapper.toResponseDTO(saved)).thenReturn(response);

        ProjectResponseDTO result = service.update(id, update);

        assertThat(result).isSameAs(response);
        verify(ownershipService).requireProjectOwner(entity);
        verify(projectMapper).updateEntityFromDTO(update, entity);
    }

    @Test
    void update_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        ProjectUpdateDTO update = mock(ProjectUpdateDTO.class);
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, update)).isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteByIdWhenExists() {
        UUID id = UUID.randomUUID();
        Project project = mock(Project.class);
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(requestRepository.existsByProjectId(id)).thenReturn(false);

        service.delete(id);

        verify(ownershipService).requireProjectOwner(project);
        verify(projectRepository).delete(project);
    }

    @Test
    void delete_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void delete_shouldThrowWhenProjectHasRequests() {
        UUID id = UUID.randomUUID();
        Project project = mock(Project.class);
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(requestRepository.existsByProjectId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(IllegalArgumentException.class);
    }
}
