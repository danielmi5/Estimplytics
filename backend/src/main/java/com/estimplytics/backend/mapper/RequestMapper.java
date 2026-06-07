package com.estimplytics.backend.mapper;

import com.estimplytics.backend.dto.RequestRequestDTO;
import com.estimplytics.backend.dto.RequestResponseDTO;
import com.estimplytics.backend.dto.RequestUpdateDTO;
import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.ProjectNotFoundException;
import com.estimplytics.backend.repository.ProjectRepository;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RequestMapper implements IMapper<Request, RequestRequestDTO, RequestResponseDTO, RequestUpdateDTO> {

    private final ProjectRepository projectRepository;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    public RequestMapper(ProjectRepository projectRepository,
                           RedmineIssueMetadataRepository redmineIssueMetadataRepository) {
        this.projectRepository = projectRepository;
        this.redmineIssueMetadataRepository = redmineIssueMetadataRepository;
    }

    @Override
    public Request toEntity(RequestRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Project not found with id %s".formatted(dto.getProjectId())));

        return toEntity(dto, project);
    }

    public Request toEntity(RequestRequestDTO dto, Project project) {
        if (dto == null) {
            return null;
        }

        Request entity = new Request();
        entity.setProject(project);
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setPriority(dto.getPriority());
        entity.setDemandType(dto.getDemandType());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDoneRatio(dto.getDoneRatio());
        entity.setEstimatedHours(dto.getEstimatedHours());
        entity.setSpentHours(dto.getSpentHours());

        return entity;
    }

    @Override
    public RequestResponseDTO toResponseDTO(Request entity) {
        if (entity == null) {
            return null;
        }
        RedmineIssueMetadata metadata = redmineIssueMetadataRepository.findByRequestId(entity.getId()).orElse(null);
        return toResponseDTO(entity, metadata);
    }

    public Page<RequestResponseDTO> toResponseDTOPage(Page<Request> page) {
        List<Request> content = page.getContent();
        if (content.isEmpty()) {
            return page.map(entity -> null);
        }
        List<UUID> ids = content.stream().map(Request::getId).toList();
        Map<UUID, RedmineIssueMetadata> metadataByRequestId = redmineIssueMetadataRepository.findByRequestIdIn(ids)
                .stream()
                .collect(Collectors.toMap(metadata -> metadata.getRequest().getId(), Function.identity(), (a, b) -> a));
        return page.map(entity -> toResponseDTO(entity, metadataByRequestId.get(entity.getId())));
    }

    private RequestResponseDTO toResponseDTO(Request entity, RedmineIssueMetadata metadata) {
        if (entity == null) {
            return null;
        }

        Project project = entity.getProject();

        RequestResponseDTO.RequestResponseDTOBuilder builder = RequestResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .demandType(entity.getDemandType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .doneRatio(entity.getDoneRatio())
                .estimatedHours(entity.getEstimatedHours())
                .spentHours(entity.getSpentHours())
                .createdDate(entity.getCreatedDate());

        if (entity.getOwner() != null) {
            builder.ownerId(entity.getOwner().getId());
        }
        if (entity.getAnalysedBy() != null) {
            builder.analysedById(entity.getAnalysedBy().getId())
                    .analysedByName(entity.getAnalysedBy().getName());
        }
        builder.analysedAt(entity.getAnalysedAt());

        if (project != null) {
            builder.projectId(project.getId())
                    .projectName(project.getName());
        }

        if (entity.getOriginRequestCode() != null) {
            builder.originRequestCode(entity.getOriginRequestCode());
        }

        if (metadata != null) {
            applyMetadata(builder, metadata, project == null);
        }

        return builder.build();
    }

    @Override
    public void updateEntityFromDTO(RequestUpdateDTO dto, Request entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    private void applyMetadata(RequestResponseDTO.RequestResponseDTOBuilder builder,
                               RedmineIssueMetadata metadata,
                               boolean includeProjectName) {
        builder.redmineId(metadata.getRedmineId())
                .assigneeName(metadata.getAssigneeName())
                .authorName(metadata.getAuthorName())
                .redmineCreatedDate(metadata.getRedmineCreatedDate())
                .redmineUpdatedDate(metadata.getRedmineUpdatedDate())
                .redmineClosedDate(metadata.getRedmineClosedDate());

        if (includeProjectName) {
            builder.projectName(metadata.getProjectName());
        }
    }
}
