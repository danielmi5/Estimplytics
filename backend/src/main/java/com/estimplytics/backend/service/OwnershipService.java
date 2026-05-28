package com.estimplytics.backend.service;

import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;

    public void requireSelfOrAdmin(UUID targetUserId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        if (isAdmin) return;

        User current = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));

        if (!current.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Access denied: resource belongs to another user");
        }
    }

    public User resolveCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
