package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ImpactAnalysisDocumentDTO;
import com.estimplytics.backend.dto.ImpactAnalysisRequestDTO;
import com.estimplytics.backend.dto.ImpactAnalysisResponseDTO;
import com.estimplytics.backend.dto.ImpactAnalysisUpdateDTO;
import com.estimplytics.backend.entity.ImpactAnalysis;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.ImpactAnalysisNotFoundException;
import com.estimplytics.backend.mapper.ImpactAnalysisMapper;
import com.estimplytics.backend.repository.ImpactAnalysisRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.RequestRepository;
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
    private final RequestRepository requestRepository;
    private final OwnershipService ownershipService;

    public ImpactAnalysisService(ImpactAnalysisRepository repository, ImpactAnalysisMapper mapper, DocxGeneratorService docxGeneratorService, RedmineIssueMetadataRepository redmineIssueMetadataRepository, RequestRepository requestRepository, OwnershipService ownershipService) {
        this.repository = repository;
        this.mapper = mapper;
        this.docxGeneratorService = docxGeneratorService;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
        this.requestRepository = requestRepository;
        this.ownershipService = ownershipService;
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
        requireEditAndRenew(entity.getRequest());
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public ImpactAnalysisResponseDTO update(UUID id, ImpactAnalysisUpdateDTO dto) {
        return repository.findById(id).map(entity -> {
            requireEditAndRenew(entity.getRequest());
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toResponseDTO(repository.save(entity));
        }).orElseThrow(() -> new ImpactAnalysisNotFoundException("ImpactAnalysis not found with id %s".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) throw new ImpactAnalysisNotFoundException("ImpactAnalysis not found with id %s".formatted(id));
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocxExport> exportDocx(UUID id) {
        return repository.findById(id).map(analysis -> {
            String code = originRequestCode(analysis);
            byte[] content = docxGeneratorService.exportImpactAnalysis(toDocument(analysis, code));
            return new DocxExport(content, "ImpactAnalysis-%s.docx".formatted(code));
        });
    }

    private void requireEditAndRenew(Request request) {
        if (request == null || request.getId() == null) throw new IllegalStateException("Impact analysis must be linked to a request");
        Request managedRequest = requestRepository.findById(request.getId()).orElseThrow(() -> new IllegalStateException("Parent request not found"));
        ownershipService.requireEditAndRenew(managedRequest);
        requestRepository.save(managedRequest);
    }

    private ImpactAnalysisDocumentDTO toDocument(ImpactAnalysis analysis, String originRequestCode) {
        var request = analysis.getRequest();
        var metadata = redmineIssueMetadataRepository.findByRequestId(request.getId());
        var documentData = analysis.getDocumentData();
        String projectCode = metadata.map(RedmineIssueMetadata::getProjectName).orElseGet(() -> request.getProject().getName());
        return new ImpactAnalysisDocumentDTO(originRequestCode, projectCode, analysis.getUpdatedAt().format(DATE_FORMAT), orEmpty(request.getDemandType()), orEmpty(analysis.getAnalyst().getName()), orEmpty(request.getPriority()), orEmpty(request.getDescription()), orEmpty(documentData.get("descripcionAbreviada")), orEmpty(documentData.get("descripcionImpacto")), orEmpty(documentData.get("descripcionSolucion")), orEmpty(documentData.get("requisitosFuncionales")), "v%02dr%02d".formatted(analysis.getVersionNumber(), 0), orEmpty(documentData.get("pruebas")));
    }

    private String originRequestCode(ImpactAnalysis analysis) {
        if (analysis.getRequest() == null) return "";
        return redmineIssueMetadataRepository.findByRequestId(analysis.getRequest().getId()).map(RedmineIssueMetadata::getOriginRequestCode).orElse("");
    }

    private static String orEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
