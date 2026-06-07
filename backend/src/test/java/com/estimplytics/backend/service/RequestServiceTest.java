package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.exception.RequestNotFoundException;
import com.estimplytics.backend.mapper.RequestMapper;
import com.estimplytics.backend.repository.ProjectRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository repository;

    @Mock
    private RequestMapper mapper;

    @Mock
    private RedmineIssueMetadataRepository redmineMetadataRepository;

    @Mock
    private OwnershipService ownershipService;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private RequestService service;

    @Test
    void findAll_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Request entity = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        User currentUser = mock(User.class);
        Page<Request> page = new PageImpl<>(List.of(entity));
        Page<RequestResponseDTO> mappedPage = new PageImpl<>(List.of(response));
        when(ownershipService.currentUser()).thenReturn(currentUser);
        when(ownershipService.redmineInstanceId(currentUser)).thenReturn(Optional.of(1L));
        when(currentUser.getId()).thenReturn(UUID.randomUUID());
        when(repository.findAllAccessible(any(), any(), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDTOPage(page)).thenReturn(mappedPage);

        Page<RequestResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findAll_shouldReturnManualRequestsWhenUserHasNoRedmineCredential() {
        Pageable pageable = PageRequest.of(0, 10);
        Request entity = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        User current = mock(User.class);
        UUID userId = UUID.randomUUID();
        Page<Request> page = new PageImpl<>(List.of(entity));
        Page<RequestResponseDTO> mappedPage = new PageImpl<>(List.of(response));
        when(ownershipService.currentUser()).thenReturn(current);
        when(current.getId()).thenReturn(userId);
        when(ownershipService.redmineInstanceId(current)).thenReturn(Optional.empty());
        when(repository.findManualAccessible(userId, pageable)).thenReturn(page);
        when(mapper.toResponseDTOPage(page)).thenReturn(mappedPage);

        Page<RequestResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findAll_shouldSearchWhenTermProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Request entity = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        User currentUser = mock(User.class);
        Page<Request> page = new PageImpl<>(List.of(entity));
        Page<RequestResponseDTO> mappedPage = new PageImpl<>(List.of(response));
        when(ownershipService.currentUser()).thenReturn(currentUser);
        when(ownershipService.redmineInstanceId(currentUser)).thenReturn(Optional.of(1L));
        when(currentUser.getId()).thenReturn(UUID.randomUUID());
        when(repository.searchAllAccessible(any(), any(), eq("portal"), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDTOPage(page)).thenReturn(mappedPage);

        Page<RequestResponseDTO> result = service.findAll(pageable, "portal");

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnMappedOptionalWhenExists() {
        UUID id = UUID.randomUUID();
        Request entity = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(ownershipService.canView(entity)).thenReturn(true);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        Optional<RequestResponseDTO> result = service.findById(id);

        assertThat(result).contains(response);
    }

    @Test
    void create_shouldPersistAndMapEntity() {
        RequestRequestDTO request = mock(RequestRequestDTO.class);
        Request entity = mock(Request.class);
        Project project = mock(Project.class);
        Request saved = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        User owner = mock(User.class);
        UUID projectId = UUID.randomUUID();
        when(request.getProjectId()).thenReturn(projectId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(project.getId()).thenReturn(projectId);
        when(project.getName()).thenReturn("Portal Clientes");
        when(repository.countManualByProjectId(projectId)).thenReturn(0L);
        when(mapper.toEntity(request, project)).thenReturn(entity);
        when(ownershipService.isAdmin()).thenReturn(false);
        when(ownershipService.currentUser()).thenReturn(owner);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        RequestResponseDTO result = service.create(request);

        assertThat(result).isSameAs(response);
        verify(ownershipService).requireOwnedProject(project);
        verify(entity).setOriginRequestCode("Portal Clientes-00001");
        verify(entity).setOwner(owner);
    }

    @Test
    void update_shouldPersistAndMapWhenEntityExists() {
        UUID id = UUID.randomUUID();
        RequestUpdateDTO update = mock(RequestUpdateDTO.class);
        Request entity = mock(Request.class);
        Request saved = mock(Request.class);
        RequestResponseDTO response = mock(RequestResponseDTO.class);
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        RequestResponseDTO result = service.update(id, update);

        assertThat(result).isSameAs(response);
        verify(ownershipService).requireRequestOwner(entity);
        verify(mapper).updateEntityFromDTO(update, entity);
    }

    @Test
    void update_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        RequestUpdateDTO update = mock(RequestUpdateDTO.class);
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, update)).isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenRedmineSourced() {
        UUID id = UUID.randomUUID();
        RequestUpdateDTO update = mock(RequestUpdateDTO.class);
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.of(mock(RedmineIssueMetadata.class)));

        assertThatThrownBy(() -> service.update(id, update)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_shouldDeleteEntityWhenExists() {
        UUID id = UUID.randomUUID();
        Request entity = mock(Request.class);
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.delete(id);

        verify(ownershipService).requireRequestOwner(entity);
        verify(repository).delete(entity);
    }

    @Test
    void delete_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void delete_shouldThrowWhenRedmineSourced() {
        UUID id = UUID.randomUUID();
        when(redmineMetadataRepository.findByRequestId(id)).thenReturn(Optional.of(mock(RedmineIssueMetadata.class)));

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(AccessDeniedException.class);
    }
}
