package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;
import com.estimplytics.backend.service.IComponentAnalysisService;
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
class ComponentAnalysisControllerTest {

    @Mock
    private IComponentAnalysisService service;

    @InjectMocks
    private ComponentAnalysisController controller;

    @Test
    void getAll_shouldReturnOkWithPage() {
        Pageable pageable = PageRequest.of(0, 10);
        ComponentAnalysisResponseDTO dto = mock(ComponentAnalysisResponseDTO.class);
        Page<ComponentAnalysisResponseDTO> page = new PageImpl<>(List.of(dto));
        when(service.findAll(pageable)).thenReturn(page);

        ResponseEntity<Page<ComponentAnalysisResponseDTO>> response = controller.getAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
    }

    @Test
    void getById_shouldReturnOkWhenExists() {
        UUID id = UUID.randomUUID();
        ComponentAnalysisResponseDTO dto = mock(ComponentAnalysisResponseDTO.class);
        when(service.findById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<ComponentAnalysisResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getById_shouldReturnNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<ComponentAnalysisResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_shouldReturnCreated() {
        ComponentAnalysisRequestDTO request = mock(ComponentAnalysisRequestDTO.class);
        ComponentAnalysisResponseDTO dto = mock(ComponentAnalysisResponseDTO.class);
        when(service.create(request)).thenReturn(dto);

        ResponseEntity<ComponentAnalysisResponseDTO> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void update_shouldReturnOk() {
        UUID id = UUID.randomUUID();
        ComponentAnalysisUpdateDTO request = mock(ComponentAnalysisUpdateDTO.class);
        ComponentAnalysisResponseDTO dto = mock(ComponentAnalysisResponseDTO.class);
        when(service.update(id, request)).thenReturn(dto);

        ResponseEntity<ComponentAnalysisResponseDTO> response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getByAnalysisId_shouldReturnOk() {
        UUID analysisId = UUID.randomUUID();
        ComponentAnalysisResponseDTO dto = mock(ComponentAnalysisResponseDTO.class);
        when(service.findByAnalysisId(analysisId)).thenReturn(List.of(dto));

        ResponseEntity<List<ComponentAnalysisResponseDTO>> response = controller.getByAnalysisId(analysisId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete(id);
    }
}
