package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.Estimation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstimationRepository extends JpaRepository<Estimation, UUID> {
    Optional<Estimation> findByAnalysis_Id(UUID analysisId);

    @Query("""
        SELECT e
        FROM Estimation e
        WHERE e.analysis.id IN :analysisIds AND e.actualHoursFeedback IS NOT NULL
    """)
    List<Estimation> findWithFeedbackByAnalysisIds(@Param("analysisIds") List<UUID> analysisIds);
}