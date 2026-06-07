package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;
import com.estimplytics.backend.service.IImpactAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactAnalysisControllerTest {

    @Mock
    private IImpactAnalysisService service;

    @InjectMocks
    private ImpactAnalysisController controller;

    @Test
    void getAll_shouldReturnOkWithPage() {
        Pageable pageable = PageRequest.of(0, 10);
        ImpactAnalysisResponseDTO dto = mock(ImpactAnalysisResponseDTO.class);
        Page<ImpactAnalysisResponseDTO> page = new PageImpl<>(List.of(dto));
        when(service.findAll(pageable)).thenReturn(page);

        ResponseEntity<Page<ImpactAnalysisResponseDTO>> response = controller.getAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
    }

    @Test
    void getById_shouldReturnOkWhenExists() {
        UUID id = UUID.randomUUID();
        ImpactAnalysisResponseDTO dto = mock(ImpactAnalysisResponseDTO.class);
        when(service.findById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getById_shouldReturnNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_shouldReturnCreated() {
        ImpactAnalysisRequestDTO request = mock(ImpactAnalysisRequestDTO.class);
        ImpactAnalysisResponseDTO dto = mock(ImpactAnalysisResponseDTO.class);
        when(service.create(request)).thenReturn(dto);

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void update_shouldReturnOk() {
        UUID id = UUID.randomUUID();
        ImpactAnalysisUpdateDTO request = mock(ImpactAnalysisUpdateDTO.class);
        ImpactAnalysisResponseDTO dto = mock(ImpactAnalysisResponseDTO.class);
        when(service.update(id, request)).thenReturn(dto);

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getByRequestId_shouldReturnOkWhenExists() {
        UUID requestId = UUID.randomUUID();
        ImpactAnalysisResponseDTO dto = mock(ImpactAnalysisResponseDTO.class);
        when(service.findByRequestId(requestId)).thenReturn(Optional.of(dto));

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.getByRequestId(requestId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getByRequestId_shouldReturnNotFoundWhenMissing() {
        UUID requestId = UUID.randomUUID();
        when(service.findByRequestId(requestId)).thenReturn(Optional.empty());

        ResponseEntity<ImpactAnalysisResponseDTO> response = controller.getByRequestId(requestId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete(id);
    }
}
