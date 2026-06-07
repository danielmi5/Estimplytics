package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.UserRequestDTO;
import com.estimplytics.backend.dto.UserResponseDTO;
import com.estimplytics.backend.dto.UserUpdateDTO;
import com.estimplytics.backend.service.IUserService;
import com.estimplytics.backend.service.OwnershipService;
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
class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private OwnershipService ownershipService;

    @InjectMocks
    private UserController userController;

    @Test
    void getAll_shouldReturnOkWithPage() {
        Pageable pageable = PageRequest.of(0, 10);
        UserResponseDTO dto = mock(UserResponseDTO.class);
        Page<UserResponseDTO> page = new PageImpl<>(List.of(dto));
        when(userService.findAll(pageable)).thenReturn(page);

        ResponseEntity<Page<UserResponseDTO>> response = userController.getAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
    }

    @Test
    void getById_shouldReturnOkWhenExists() {
        UUID id = UUID.randomUUID();
        UserResponseDTO dto = mock(UserResponseDTO.class);
        when(userService.findById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<UserResponseDTO> response = userController.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getById_shouldReturnNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<UserResponseDTO> response = userController.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_shouldReturnCreated() {
        UserRequestDTO request = mock(UserRequestDTO.class);
        UserResponseDTO dto = mock(UserResponseDTO.class);
        when(userService.create(request)).thenReturn(dto);

        ResponseEntity<UserResponseDTO> response = userController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void update_shouldReturnOk() {
        UUID id = UUID.randomUUID();
        UserUpdateDTO request = mock(UserUpdateDTO.class);
        UserResponseDTO dto = mock(UserResponseDTO.class);
        when(userService.update(id, request)).thenReturn(dto);

        ResponseEntity<UserResponseDTO> response = userController.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = userController.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).delete(id);
    }
}
