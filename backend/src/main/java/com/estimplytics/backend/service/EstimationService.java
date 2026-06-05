package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import com.estimplytics.backend.dto.EstimationAlgorithmResultDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.EstimationNotFoundException;
import com.estimplytics.backend.mapper.EstimationMapper;
import com.estimplytics.backend.repository.EstimationRepository;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
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
    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final RequestRepository requestRepository;
    private final OwnershipService ownershipService;

    public EstimationService(EstimationRepository repository, EstimationMapper mapper, EstimationAlgorithmService estimationAlgorithmService, ExcelGeneratorService excelGeneratorService, RedmineIssueMetadataRepository redmineIssueMetadataRepository, ImpactAnalysisRepository impactAnalysisRepository, RequestRepository requestRepository, OwnershipService ownershipService) {
        this.repository = repository;
        this.mapper = mapper;
        this.estimationAlgorithmService = estimationAlgorithmService;
        this.excelGeneratorService = excelGeneratorService;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
        this.impactAnalysisRepository = impactAnalysisRepository;
        this.requestRepository = requestRepository;
        this.ownershipService = ownershipService;
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
        requireEditAndRenew(entity);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public EstimationResponseDTO update(UUID id, EstimationUpdateDTO dto) {
        return repository.findById(id).map(entity -> {
            requireEditAndRenew(entity);
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toResponseDTO(repository.save(entity));
        }).orElseThrow(() -> new EstimationNotFoundException("Estimation not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) throw new EstimationNotFoundException("Estimation not found with id %s".formatted(id));
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExcelExport> exportExcel(UUID id) {
        return repository.findById(id).map(estimation -> {
            String code = originRequestCode(estimation);
            byte[] content = excelGeneratorService.exportEstimation(estimation, code);
            return new ExcelExport(content, "Estimation-%s.xlsx".formatted(code));
        });
    }

    private void requireEditAndRenew(Estimation estimation) {
        Request request = parentRequest(estimation);
        ownershipService.requireEditAndRenew(request);
        requestRepository.save(request);
    }

    private Request parentRequest(Estimation estimation) {
        if (estimation.getAnalysis() == null || estimation.getAnalysis().getId() == null) {
            throw new IllegalStateException("Estimation must be linked to an impact analysis");
        }
        return impactAnalysisRepository.findById(estimation.getAnalysis().getId()).map(analysis -> {
            if (analysis.getRequest() == null || analysis.getRequest().getId() == null) {
                throw new IllegalStateException("Impact analysis must be linked to a request");
            }
            return requestRepository.findById(analysis.getRequest().getId()).orElseThrow(() -> new IllegalStateException("Parent request not found"));
        }).orElseThrow(() -> new IllegalStateException("Impact analysis not found"));
    }

    private String originRequestCode(Estimation estimation) {
        if (estimation.getAnalysis() == null || estimation.getAnalysis().getRequest() == null) return "";
        UUID requestId = estimation.getAnalysis().getRequest().getId();
        return redmineIssueMetadataRepository.findByRequestId(requestId).map(RedmineIssueMetadata::getOriginRequestCode).orElse("");
    }
}
