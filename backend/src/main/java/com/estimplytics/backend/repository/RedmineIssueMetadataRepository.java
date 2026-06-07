package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.RedmineIssueMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RedmineIssueMetadataRepository extends JpaRepository<RedmineIssueMetadata, Long> {
    Optional<RedmineIssueMetadata> findByRedmineIdAndRedmineInstanceId(Integer redmineId, Long instanceId);
    Optional<RedmineIssueMetadata> findByRequestId(UUID requestId);

    @Query("SELECT m FROM RedmineIssueMetadata m WHERE m.request.id IN :requestIds")
    List<RedmineIssueMetadata> findByRequestIdIn(@Param("requestIds") Collection<UUID> requestIds);
}
