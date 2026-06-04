package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import com.estimplytics.backend.dto.EstimationAlgorithmResultDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.exception.EstimationNotFoundException;
import com.estimplytics.backend.mapper.EstimationMapper;
import com.estimplytics.backend.repository.EstimationRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class EstimationService implements IEstimationService {

    private final EstimationRepository repository;
    private final EstimationMapper mapper;
    private final EstimationAlgorithmService estimationAlgorithmService;
    private final ExcelGeneratorService excelGeneratorService;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    public EstimationService(EstimationRepository repository, EstimationMapper mapper, EstimationAlgorithmService estimationAlgorithmService, ExcelGeneratorService excelGeneratorService, RedmineIssueMetadataRepository redmineIssueMetadataRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.estimationAlgorithmService = estimationAlgorithmService;
        this.excelGeneratorService = excelGeneratorService;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
    }

    @Override
    public Page<EstimationResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Override
    public Optional<EstimationResponseDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public EstimationResponseDTO create(EstimationRequestDTO dto) {
        EstimationAlgorithmResultDTO estimationAlgorithmResult = estimationAlgorithmService.calculateSuggestionForAnalysisId(dto.getAnalysisId());
        dto.setHoursPlanning(estimationAlgorithmResult.getSuggestedHoursPlanning());
        dto.setHoursAnalysis(estimationAlgorithmResult.getSuggestedHoursAnalysis());
        dto.setHoursDevelopment(estimationAlgorithmResult.getSuggestedHoursDevelopment());
        dto.setHoursTesting(estimationAlgorithmResult.getSuggestedHoursTesting());
        dto.setTotalHours(estimationAlgorithmResult.getSuggestedTotalHours());
        dto.setFiability(estimationAlgorithmResult.getFiabilityPercentage());
        Estimation entity = mapper.toEntity(dto);
        Estimation savedEntity = repository.save(entity);
        return mapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public EstimationResponseDTO update(UUID id, EstimationUpdateDTO dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toResponseDTO(repository.save(entity));
        }).orElseThrow(() -> new EstimationNotFoundException("Estimation not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EstimationNotFoundException("Estimation not found with id %s".formatted(id));
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExcelExport> exportExcel(UUID id) {
        return repository.findById(id).map(estimation -> {
            String originRequestCode = resolveOriginRequestCode(estimation);
            byte[] content = excelGeneratorService.exportEstimation(estimation, originRequestCode);
            String filename = "Estimation-" + originRequestCode + ".xlsx";
            return new ExcelExport(content, filename);
        });
    }

    private String resolveOriginRequestCode(Estimation estimation) {
        if (estimation.getAnalysis() == null || estimation.getAnalysis().getRequest() == null) {
            return "";
        }
        UUID requestId = estimation.getAnalysis().getRequest().getId();
        return redmineIssueMetadataRepository.findByRequestId(requestId)
            .map(RedmineIssueMetadata::getOriginRequestCode)
            .orElse("");
    }
}