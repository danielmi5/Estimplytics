package com.estimplytics.backend.mapper;

import com.estimplytics.backend.dto.ProjectRequestDTO;
import com.estimplytics.backend.dto.ProjectResponseDTO;
import com.estimplytics.backend.dto.ProjectUpdateDTO;
import com.estimplytics.backend.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper implements IMapper<Project, ProjectRequestDTO, ProjectResponseDTO, ProjectUpdateDTO> {

    @Override
    public Project toEntity(ProjectRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    @Override
    public ProjectResponseDTO toResponseDTO(Project entity) {
        if (entity == null) {
            return null;
        }

        return ProjectResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .build();
    }

    @Override
    public void updateEntityFromDTO(ProjectUpdateDTO dto, Project entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
