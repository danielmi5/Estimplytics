package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.RequestNotFoundException;
import com.estimplytics.backend.mapper.RequestMapper;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RequestService implements IRequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final RedmineIssueMetadataRepository redmineMetadataRepository;

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper, RedmineIssueMetadataRepository redmineMetadataRepository) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.redmineMetadataRepository = redmineMetadataRepository;
    }

    private void rejectIfRedmineSourced(UUID id) {
        if (redmineMetadataRepository.findByRequestId(id).isPresent()) {
            throw new AccessDeniedException("Redmine requests cannot be modified or deleted");
        }
    }

    @Override
    public Page<RequestResponseDTO> findAll(Pageable pageable) {
        return findAll(pageable, null);
    }

    @Override
    public Page<RequestResponseDTO> findAll(Pageable pageable, String search) {
        Page<Request> page = search == null || search.isBlank()
                ? requestRepository.findAll(pageable)
                : requestRepository.searchAll(search.trim(), pageable);
        return requestMapper.toResponseDTOPage(page);
    }

    @Override
    public Optional<RequestResponseDTO> findById(UUID id) {
        return requestRepository.findById(id).map(requestMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public RequestResponseDTO create(RequestRequestDTO dto) {
        Request entity = requestMapper.toEntity(dto);
        Request savedEntity = requestRepository.save(entity);
        return requestMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public RequestResponseDTO update(UUID id, RequestUpdateDTO dto) {
        rejectIfRedmineSourced(id);
        return requestRepository.findById(id).map(entity -> {
            requestMapper.updateEntityFromDTO(dto, entity);
            return requestMapper.toResponseDTO(requestRepository.save(entity));
        }).orElseThrow(() -> new RequestNotFoundException("Request not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        rejectIfRedmineSourced(id);
        if (!requestRepository.existsById(id)) {
            throw new RequestNotFoundException("Request not found with id %s".formatted(id));
        }
        requestRepository.deleteById(id);
    }
}
