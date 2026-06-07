package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.ComponentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComponentAnalysisRepository extends JpaRepository<ComponentAnalysis, UUID> {
    List<ComponentAnalysis> findByAnalysis_Id(UUID analysisId);

    @Query("SELECT ca.component.id FROM ComponentAnalysis ca WHERE ca.analysis.id = :analysisId")
    List<UUID> findComponentIdsByAnalysisId(@Param("analysisId") UUID analysisId);

    @Query("""
        SELECT ca.analysis.id
        FROM ComponentAnalysis ca
        GROUP BY ca.analysis.id
        HAVING COUNT(DISTINCT ca.component.id) = :componentCount AND SUM(CASE WHEN ca.component.id IN :componentIds THEN 1 ELSE 0 END) = :componentCount
    """)
    List<UUID> findAnalysisIdsWithExactComponentSet(@Param("componentIds") List<UUID> componentIds, @Param("componentCount") long componentCount);
}