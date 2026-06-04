package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOwnerOrOwnerIsNull(User owner, Pageable pageable);
}
