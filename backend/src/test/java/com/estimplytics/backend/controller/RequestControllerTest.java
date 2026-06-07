package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.service.IRequestService;
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
class RequestControllerTest {

    @Mock
    private IRequestService service;

    @InjectMocks
    private RequestController controller;

    @Test
    void getAll_shouldReturnOkWithPage() {
        Pageable pageable = PageRequest.of(0, 10);
        RequestResponseDTO dto = mock(RequestResponseDTO.class);
        Page<RequestResponseDTO> page = new PageImpl<>(List.of(dto));
        when(service.findAll(pageable, null)).thenReturn(page);

        ResponseEntity<Page<RequestResponseDTO>> response = controller.getAll(pageable, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
    }

    @Test
    void getAll_shouldPassSearchParamToService() {
        Pageable pageable = PageRequest.of(0, 10);
        RequestResponseDTO dto = mock(RequestResponseDTO.class);
        Page<RequestResponseDTO> page = new PageImpl<>(List.of(dto));
        when(service.findAll(pageable, "facturación")).thenReturn(page);

        ResponseEntity<Page<RequestResponseDTO>> response = controller.getAll(pageable, "facturación");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
        verify(service).findAll(pageable, "facturación");
    }

    @Test
    void getById_shouldReturnOkWhenExists() {
        UUID id = UUID.randomUUID();
        RequestResponseDTO dto = mock(RequestResponseDTO.class);
        when(service.findById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<RequestResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getById_shouldReturnNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<RequestResponseDTO> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_shouldReturnCreated() {
        RequestRequestDTO request = mock(RequestRequestDTO.class);
        RequestResponseDTO dto = mock(RequestResponseDTO.class);
        when(service.create(request)).thenReturn(dto);

        ResponseEntity<RequestResponseDTO> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void update_shouldReturnOk() {
        UUID id = UUID.randomUUID();
        RequestUpdateDTO request = mock(RequestUpdateDTO.class);
        RequestResponseDTO dto = mock(RequestResponseDTO.class);
        when(service.update(id, request)).thenReturn(dto);

        ResponseEntity<RequestResponseDTO> response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete(id);
    }
}
