package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {
    boolean existsByProjectId(UUID projectId);

    @Query("""
            SELECT DISTINCT r FROM Request r
            LEFT JOIN RedmineIssueMetadata m ON m.request = r
            LEFT JOIN r.project p
            WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(p.name, m.projectName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(m.originRequestCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Request> searchAll(@Param("search") String search, Pageable pageable);
}
