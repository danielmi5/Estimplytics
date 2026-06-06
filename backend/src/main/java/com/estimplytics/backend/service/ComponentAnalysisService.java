package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ComponentAnalysisRequestDTO;
import com.estimplytics.backend.dto.ComponentAnalysisResponseDTO;
import com.estimplytics.backend.dto.ComponentAnalysisUpdateDTO;
import com.estimplytics.backend.entity.ComponentAnalysis;
import com.estimplytics.backend.exception.ComponentAnalysisNotFoundException;
import com.estimplytics.backend.mapper.ComponentAnalysisMapper;
import com.estimplytics.backend.repository.ComponentAnalysisRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ComponentAnalysisService implements IComponentAnalysisService {

    private final ComponentAnalysisRepository repository;
    private final ComponentAnalysisMapper mapper;

    public ComponentAnalysisService(ComponentAnalysisRepository repository, ComponentAnalysisMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<ComponentAnalysisResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Override
    public Optional<ComponentAnalysisResponseDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public ComponentAnalysisResponseDTO create(ComponentAnalysisRequestDTO dto) {
        ComponentAnalysis entity = mapper.toEntity(dto);
        ComponentAnalysis savedEntity = repository.save(entity);
        return mapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public ComponentAnalysisResponseDTO update(UUID id, ComponentAnalysisUpdateDTO dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toResponseDTO(repository.save(entity));
        }).orElseThrow(() -> new ComponentAnalysisNotFoundException("ComponentAnalysis not found with id %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentAnalysisResponseDTO> findByAnalysisId(UUID analysisId) {
        return repository.findByAnalysis_Id(analysisId).stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ComponentAnalysisNotFoundException("ComponentAnalysis not found with id %s".formatted(id));
        }
        repository.deleteById(id);
    }
}