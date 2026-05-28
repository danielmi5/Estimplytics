package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.RedmineInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedmineInstanceRepository extends JpaRepository<RedmineInstance, Long> {
    Optional<RedmineInstance> findByBaseUrl(String baseUrl);
}
