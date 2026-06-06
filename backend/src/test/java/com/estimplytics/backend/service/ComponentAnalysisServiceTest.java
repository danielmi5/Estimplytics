package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;
import com.estimplytics.backend.entity.ComponentAnalysis;
import com.estimplytics.backend.exception.ComponentAnalysisNotFoundException;
import com.estimplytics.backend.mapper.ComponentAnalysisMapper;
import com.estimplytics.backend.repository.ComponentAnalysisRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentAnalysisServiceTest {

    @Mock
    private ComponentAnalysisRepository repository;

    @Mock
    private ComponentAnalysisMapper mapper;

    @InjectMocks
    private ComponentAnalysisService service;

    @Test
    void findAll_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        ComponentAnalysis entity = mock(ComponentAnalysis.class);
        ComponentAnalysisResponseDTO response = mock(ComponentAnalysisResponseDTO.class);
        Page<ComponentAnalysis> page = new PageImpl<>(List.of(entity));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        Page<ComponentAnalysisResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnMappedOptionalWhenExists() {
        UUID id = UUID.randomUUID();
        ComponentAnalysis entity = mock(ComponentAnalysis.class);
        ComponentAnalysisResponseDTO response = mock(ComponentAnalysisResponseDTO.class);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        Optional<ComponentAnalysisResponseDTO> result = service.findById(id);

        assertThat(result).contains(response);
    }

    @Test
    void create_shouldPersistAndMapEntity() {
        ComponentAnalysisRequestDTO request = mock(ComponentAnalysisRequestDTO.class);
        ComponentAnalysis entity = mock(ComponentAnalysis.class);
        ComponentAnalysis saved = mock(ComponentAnalysis.class);
        ComponentAnalysisResponseDTO response = mock(ComponentAnalysisResponseDTO.class);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        ComponentAnalysisResponseDTO result = service.create(request);

        assertThat(result).isSameAs(response);
    }

    @Test
    void update_shouldPersistAndMapWhenEntityExists() {
        UUID id = UUID.randomUUID();
        ComponentAnalysisUpdateDTO update = mock(ComponentAnalysisUpdateDTO.class);
        ComponentAnalysis entity = mock(ComponentAnalysis.class);
        ComponentAnalysis saved = mock(ComponentAnalysis.class);
        ComponentAnalysisResponseDTO response = mock(ComponentAnalysisResponseDTO.class);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        ComponentAnalysisResponseDTO result = service.update(id, update);

        assertThat(result).isSameAs(response);
        verify(mapper).updateEntityFromDTO(update, entity);
    }

    @Test
    void update_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        ComponentAnalysisUpdateDTO update = mock(ComponentAnalysisUpdateDTO.class);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, update)).isInstanceOf(ComponentAnalysisNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteByIdWhenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void delete_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ComponentAnalysisNotFoundException.class);
    }

    @Test
    void findByAnalysisId_shouldMapEntities() {
        UUID analysisId = UUID.randomUUID();
        ComponentAnalysis entity = mock(ComponentAnalysis.class);
        ComponentAnalysisResponseDTO response = mock(ComponentAnalysisResponseDTO.class);
        when(repository.findByAnalysis_Id(analysisId)).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        List<ComponentAnalysisResponseDTO> result = service.findByAnalysisId(analysisId);

        assertThat(result).containsExactly(response);
    }
}
