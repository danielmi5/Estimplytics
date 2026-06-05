package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import com.estimplytics.backend.dto.EstimationAlgorithmResultDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.ImpactAnalysis;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.EstimationNotFoundException;
import com.estimplytics.backend.mapper.EstimationMapper;
import com.estimplytics.backend.repository.EstimationRepository;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
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
class EstimationServiceTest {

    @Mock
    private EstimationRepository repository;

    @Mock
    private EstimationMapper mapper;

    @Mock
    private EstimationAlgorithmService estimationAlgorithmService;

    @Mock
    private ExcelGeneratorService excelGeneratorService;

    @Mock
    private ImpactAnalysisRepository impactAnalysisRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private OwnershipService ownershipService;

    @InjectMocks
    private EstimationService service;

    @Test
    void findAll_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Estimation entity = mock(Estimation.class);
        EstimationResponseDTO response = mock(EstimationResponseDTO.class);
        Page<Estimation> page = new PageImpl<>(List.of(entity));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        Page<EstimationResponseDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnMappedOptionalWhenExists() {
        UUID id = UUID.randomUUID();
        Estimation entity = mock(Estimation.class);
        EstimationResponseDTO response = mock(EstimationResponseDTO.class);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        Optional<EstimationResponseDTO> result = service.findById(id);

        assertThat(result).contains(response);
    }

    @Test
    void create_shouldPersistAndMapEntity() {
        UUID analysisId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        ImpactAnalysis analysis = mock(ImpactAnalysis.class);
        Request request = mock(Request.class);
        Request managedRequest = mock(Request.class);
        EstimationRequestDTO requestDto = mock(EstimationRequestDTO.class);
        Estimation entity = mock(Estimation.class);
        Estimation saved = mock(Estimation.class);
        EstimationResponseDTO response = mock(EstimationResponseDTO.class);
        when(requestDto.getAnalysisId()).thenReturn(analysisId);
        EstimationAlgorithmResultDTO algorithmResult = EstimationAlgorithmResultDTO.builder()
                .suggestedTotalHours(20)
                .fiabilityPercentage(80)
                .build();
        when(estimationAlgorithmService.calculateSuggestionForAnalysisId(analysisId)).thenReturn(algorithmResult);
        when(mapper.toEntity(requestDto)).thenReturn(entity);
        when(entity.getAnalysis()).thenReturn(analysis);
        when(analysis.getId()).thenReturn(analysisId);
        when(impactAnalysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));
        when(analysis.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn(requestId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(managedRequest));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        EstimationResponseDTO result = service.create(requestDto);

        assertThat(result).isSameAs(response);
        verify(ownershipService).requireEditAndRenew(managedRequest);
    }

    @Test
    void update_shouldPersistAndMapWhenEntityExists() {
        UUID id = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        ImpactAnalysis analysis = mock(ImpactAnalysis.class);
        Request request = mock(Request.class);
        Request managedRequest = mock(Request.class);
        EstimationUpdateDTO update = mock(EstimationUpdateDTO.class);
        Estimation entity = mock(Estimation.class);
        Estimation saved = mock(Estimation.class);
        EstimationResponseDTO response = mock(EstimationResponseDTO.class);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(entity.getAnalysis()).thenReturn(analysis);
        when(analysis.getId()).thenReturn(analysisId);
        when(impactAnalysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));
        when(analysis.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn(requestId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(managedRequest));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        EstimationResponseDTO result = service.update(id, update);

        assertThat(result).isSameAs(response);
        verify(mapper).updateEntityFromDTO(update, entity);
    }

    @Test
    void update_shouldThrowWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        EstimationUpdateDTO update = mock(EstimationUpdateDTO.class);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, update)).isInstanceOf(EstimationNotFoundException.class);
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

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(EstimationNotFoundException.class);
    }
}
