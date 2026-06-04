package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.UserRequestDTO;
import com.estimplytics.backend.dto.UserResponseDTO;
import com.estimplytics.backend.dto.UserUpdateDTO;
import com.estimplytics.backend.service.IUserService;
import com.estimplytics.backend.service.OwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.estimplytics.backend.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
public class UserController implements IUserController {

    private final IUserService userService;
    private final OwnershipService ownershipService;

    public UserController(IUserService userService, OwnershipService ownershipService) {
        this.userService = userService;
        this.ownershipService = ownershipService;
    }

    @Override
    public ResponseEntity<Page<UserResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Override
    public ResponseEntity<UserResponseDTO> getById(UUID id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<UserResponseDTO> create(UserRequestDTO request) {
        UserResponseDTO response = userService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserResponseDTO> update(UUID id, UserUpdateDTO request) {
        ownershipService.requireUserOrAdmin(id);
        return ResponseEntity.ok(userService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}