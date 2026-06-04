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
            LEFT JOIN r.redmineMetadata m
            LEFT JOIN r.project p
            WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(p.name, m.projectName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(m.originRequestCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Request> searchAll(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Request r
            LEFT JOIN r.redmineMetadata m
            LEFT JOIN r.project p
            LEFT JOIN r.owner ro
            LEFT JOIN p.owner po
            WHERE m IS NULL AND (ro.id = :userId OR po.id = :userId)
            """)
    Page<Request> findManualAccessible(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Request r
            LEFT JOIN r.redmineMetadata m
            LEFT JOIN r.project p
            LEFT JOIN r.owner ro
            LEFT JOIN p.owner po
            WHERE m IS NULL AND (ro.id = :userId OR po.id = :userId)
            AND (
                LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Request> searchManualAccessible(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Request r
            LEFT JOIN r.redmineMetadata m
            LEFT JOIN r.project p
            LEFT JOIN r.owner ro
            LEFT JOIN p.owner po
            WHERE (m IS NULL AND (ro.id = :userId OR po.id = :userId))
            OR (m IS NOT NULL AND m.redmineInstance.id = :instanceId)
            """)
    Page<Request> findAllAccessible(@Param("userId") UUID userId, @Param("instanceId") Long instanceId, Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Request r
            LEFT JOIN r.redmineMetadata m
            LEFT JOIN r.project p
            LEFT JOIN r.owner ro
            LEFT JOIN p.owner po
            WHERE (
                LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(p.name, m.projectName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.originRequestCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                (m IS NULL AND (ro.id = :userId OR po.id = :userId))
                OR (m IS NOT NULL AND m.redmineInstance.id = :instanceId)
            )
            """)
    Page<Request> searchAllAccessible(@Param("userId") UUID userId, @Param("instanceId") Long instanceId, @Param("search") String search, Pageable pageable);
}
