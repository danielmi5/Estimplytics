package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.ImpactAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImpactAnalysisRepository extends JpaRepository<ImpactAnalysis, UUID> {
    Optional<ImpactAnalysis> findByRequest_Id(UUID requestId);
}