package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ImpactAnalysisDocumentDTO;
import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;
import com.estimplytics.backend.entity.ImpactAnalysis;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.exception.ImpactAnalysisNotFoundException;
import com.estimplytics.backend.mapper.ImpactAnalysisMapper;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImpactAnalysisService implements IImpactAnalysisService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ImpactAnalysisRepository repository;
    private final ImpactAnalysisMapper mapper;
    private final DocxGeneratorService docxGeneratorService;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    public ImpactAnalysisService(ImpactAnalysisRepository repository, ImpactAnalysisMapper mapper, DocxGeneratorService docxGeneratorService, RedmineIssueMetadataRepository redmineIssueMetadataRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.docxGeneratorService = docxGeneratorService;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
    }

    @Override
    public Page<ImpactAnalysisResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Override
    public Optional<ImpactAnalysisResponseDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public ImpactAnalysisResponseDTO create(ImpactAnalysisRequestDTO dto) {
        ImpactAnalysis entity = mapper.toEntity(dto);
        ImpactAnalysis savedEntity = repository.save(entity);
        return mapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public ImpactAnalysisResponseDTO update(UUID id, ImpactAnalysisUpdateDTO dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toResponseDTO(repository.save(entity));
        }).orElseThrow(() -> new ImpactAnalysisNotFoundException("ImpactAnalysis not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ImpactAnalysisNotFoundException("ImpactAnalysis not found with id %s".formatted(id));
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocxExport> exportDocx(UUID id) {
        return repository.findById(id).map(analysis -> {
            String originRequestCode = resolveOriginRequestCode(analysis);
            byte[] content = docxGeneratorService.exportImpactAnalysis(toDocument(analysis, originRequestCode));
            return new DocxExport(content, "ImpactAnalysis-%s.docx".formatted(originRequestCode));
        });
    }

    private ImpactAnalysisDocumentDTO toDocument(ImpactAnalysis analysis, String originRequestCode) {
        var request = analysis.getRequest();
        var metadata = redmineIssueMetadataRepository.findByRequestId(request.getId());
        var documentData = analysis.getDocumentData();
        String projectCode = metadata.map(RedmineIssueMetadata::getProjectName).orElseGet(() -> request.getProject().getName());

        return new ImpactAnalysisDocumentDTO(
                originRequestCode,
                projectCode,
                analysis.getUpdatedAt().format(DATE_FORMAT),
                orEmpty(request.getDemandType()),
                orEmpty(analysis.getAnalyst().getName()),
                orEmpty(request.getPriority()),
                orEmpty(request.getDescription()),
                orEmpty(documentData.get("descripcionAbreviada")),
                orEmpty(documentData.get("descripcionImpacto")),
                orEmpty(documentData.get("descripcionSolucion")),
                orEmpty(documentData.get("requisitosFuncionales")),
                "v%02dr%02d".formatted(analysis.getVersionNumber(), 0),
                orEmpty(documentData.get("pruebas"))
        );
    }

    private String resolveOriginRequestCode(ImpactAnalysis analysis) {
        if (analysis.getRequest() == null) {
            return "";
        }
        return redmineIssueMetadataRepository.findByRequestId(analysis.getRequest().getId())
                .map(RedmineIssueMetadata::getOriginRequestCode)
                .orElse("");
    }

    private static String orEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
