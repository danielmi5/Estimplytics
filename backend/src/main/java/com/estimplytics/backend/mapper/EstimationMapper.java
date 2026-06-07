package com.estimplytics.backend.mapper;

import com.estimplytics.backend.dto.EstimationRequestDTO;
import com.estimplytics.backend.dto.EstimationResponseDTO;
import com.estimplytics.backend.dto.EstimationUpdateDTO;
import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.entity.ImpactAnalysis;
import org.springframework.stereotype.Component;

@Component
public class EstimationMapper implements IMapper<Estimation, EstimationRequestDTO, EstimationResponseDTO, EstimationUpdateDTO> {

    @Override
    public Estimation toEntity(EstimationRequestDTO dto) {
        if (dto == null) return null;
        Estimation estimation = new Estimation();
        if (dto.getVersionNumber() != null) {
            estimation.setVersionNumber(dto.getVersionNumber());
        }
        estimation.setFiability(dto.getFiability());
        estimation.setHoursPlanning(dto.getHoursPlanning());
        estimation.setHoursAnalysis(dto.getHoursAnalysis());
        estimation.setHoursDevelopment(dto.getHoursDevelopment());
        estimation.setHoursTesting(dto.getHoursTesting());
        estimation.setTotalHours(dto.getTotalHours());
        estimation.setActualHoursFeedback(dto.getActualHoursFeedback());
        estimation.setJustification(dto.getJustification());
        estimation.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getAnalysisId() != null) {
            ImpactAnalysis analysis = new ImpactAnalysis();
            analysis.setId(dto.getAnalysisId());
            estimation.setAnalysis(analysis);
        }
        return estimation;
    }

    @Override
    public EstimationResponseDTO toResponseDTO(Estimation entity) {
        if (entity == null) return null;
        return EstimationResponseDTO.builder()
                .id(entity.getId())
                .analysisId(entity.getAnalysis() != null ? entity.getAnalysis().getId() : null)
                .versionNumber(entity.getVersionNumber())
                .fiability(entity.getFiability())
                .hoursPlanning(entity.getHoursPlanning())
                .hoursAnalysis(entity.getHoursAnalysis())
                .hoursDevelopment(entity.getHoursDevelopment())
                .hoursTesting(entity.getHoursTesting())
                .totalHours(entity.getTotalHours())
                .actualHoursFeedback(entity.getActualHoursFeedback())
                .justification(entity.getJustification())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public void updateEntityFromDTO(EstimationUpdateDTO dto, Estimation entity) {
        if (dto == null || entity == null) return;
        if (dto.getVersionNumber() != null) {
            entity.setVersionNumber(dto.getVersionNumber());
        }
        if (dto.getFiability() != null) {
            entity.setFiability(dto.getFiability());
        }
        if (dto.getHoursPlanning() != null) {
            entity.setHoursPlanning(dto.getHoursPlanning());
        }
        if (dto.getHoursAnalysis() != null) {
            entity.setHoursAnalysis(dto.getHoursAnalysis());
        }
        if (dto.getHoursDevelopment() != null) {
            entity.setHoursDevelopment(dto.getHoursDevelopment());
        }
        if (dto.getHoursTesting() != null) {
            entity.setHoursTesting(dto.getHoursTesting());
        }
        if (dto.getTotalHours() != null) {
            entity.setTotalHours(dto.getTotalHours());
        }
        if (dto.getActualHoursFeedback() != null) {
            entity.setActualHoursFeedback(dto.getActualHoursFeedback());
        }
        if (dto.getJustification() != null) {
            entity.setJustification(dto.getJustification());
        }
        if (dto.getUpdatedAt() != null) {
            entity.setUpdatedAt(dto.getUpdatedAt());
        }
    }
}