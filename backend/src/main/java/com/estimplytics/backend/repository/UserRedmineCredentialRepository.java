package com.estimplytics.backend.repository;

import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.entity.UserRedmineCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRedmineCredentialRepository extends JpaRepository<UserRedmineCredential, Long> {
    Optional<UserRedmineCredential> findByUser(User user);

    @Query("SELECT c.redmineInstance.id FROM UserRedmineCredential c WHERE c.user.id = :userId")
    Optional<Long> findRedmineInstanceIdByUserId(@Param("userId") UUID userId);
}
