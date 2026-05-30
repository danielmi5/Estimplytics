package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IRequestService extends ICrudService<RequestRequestDTO, RequestResponseDTO, RequestUpdateDTO, UUID> {
    Page<RequestResponseDTO> findAll(Pageable pageable, String search);
}
