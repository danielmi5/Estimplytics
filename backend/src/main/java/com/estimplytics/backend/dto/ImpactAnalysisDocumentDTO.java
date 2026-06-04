package com.estimplytics.backend.dto;

public record ImpactAnalysisDocumentDTO(
        String requestCode,
        String projectCode,
        String date,
        String demandType,
        String documentAuthor,
        String priority,
        String requestDescription,
        String briefDescription,
        String impactDescription,
        String solutionDescription,
        String functionalRequirements,
        String version,
        String tests
) {}
