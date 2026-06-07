package com.estimplytics.backend.service;

import com.estimplytics.backend.entity.Project;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.repository.RedmineIssueMetadataRepository;
import com.estimplytics.backend.repository.UserRedmineCredentialRepository;
import com.estimplytics.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Checks who can view or change resources for the logged-in user.
 * Manual requests are private. Redmine requests are shared by instance credentials.
 * Analysis and estimations need the request edit lock (see LOCK_TIMEOUT).
 * Spring Security is read here; other services call these methods.
 */
@Service
@RequiredArgsConstructor
public class OwnershipService {

    /**
     * How long the edit lock on a request lasts.
     */
    public static final Duration LOCK_TIMEOUT = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final UserRedmineCredentialRepository credentialRepository;
    private final RedmineIssueMetadataRepository redmineIssueMetadataRepository;

    /**
     * Only the same user or an ADMIN can access the resource.
     *
     * @param userId owner user id
     * @throws AccessDeniedException when another analyst tries to access it
     */
    public void requireUserOrAdmin(UUID userId) {
        if (isAdmin()) return;
        User current = currentUser();
        if (!current.getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: resource belongs to another user");
        }
    }

    /**
     * To update or delete a project: ADMIN always, or the owner for manual projects.
     * Global dictionary projects without owner can only be changed by ADMIN.
     *
     * @param project project to check
     * @throws AccessDeniedException when an analyst tries to change someone else's project
     */
    public void requireProjectOwner(Project project) {
        if (isAdmin()) return;
        User current = currentUser();
        if (project.getOwner() == null) {
            throw new AccessDeniedException("Global dictionary projects can only be modified by administrators");
        }
        if (!project.getOwner().getId().equals(current.getId())) {
            throw new AccessDeniedException("Access denied: project belongs to another user");
        }
    }

    /**
     * Checks the analyst owns the project before creating manual requests on it.
     *
     * @param project project where the request will be created
     * @throws AccessDeniedException when the analyst is not the project owner
     */
    public void requireOwnedProject(Project project) {
        if (isAdmin()) return;
        User current = currentUser();
        if (project.getOwner() == null || !project.getOwner().getId().equals(current.getId())) {
            throw new AccessDeniedException("Cannot manage requests on this project");
        }
    }

    /**
     * For updating or deleting a manual request (not Redmine).
     * Request owner or project owner is allowed.
     *
     * @param request manual request to change
     * @throws AccessDeniedException when the user is not allowed to change it
     */
    public void requireRequestOwner(Request request) {
        if (isAdmin()) return;
        User current = currentUser();
        if (request.getOwner() != null && request.getOwner().getId().equals(current.getId())) return;
        Project project = request.getProject();
        if (project != null && project.getOwner() != null && project.getOwner().getId().equals(current.getId())) return;
        throw new AccessDeniedException("Access denied: request belongs to another user");
    }

    /**
     * Same as canView but throws if access is denied.
     *
     * @param request request to check
     * @throws AccessDeniedException when the user cannot see the request
     */
    public void requireView(Request request) {
        if (!canView(request)) {
            throw new AccessDeniedException("Access denied: request is not visible to the current user");
        }
    }

    /**
     * Whether the current user can see the request in lists or detail.
     * Redmine requests need credentials for that instance. Manual requests need ownership.
     *
     * @param request request to check
     * @return true if the current user can view the request
     */
    public boolean canView(Request request) {
        User current = currentUser();
        RedmineIssueMetadata metadata = redmineIssueMetadataRepository.findByRequestId(request.getId()).orElse(null);
        if (metadata != null) {
            return credentialRepository.findRedmineInstanceIdByUserId(current.getId())
                    .map(instanceId -> metadata.getRedmineInstance() != null && metadata.getRedmineInstance().getId().equals(instanceId))
                    .orElse(false);
        }
        return ownsManualRequest(request, current);
    }

    /**
     * For editing analysis or estimations: needs request access and a free lock or lock held by current user.
     * ADMIN can edit even if someone else holds the lock.
     *
     * @param request request to check
     * @return true if the current user can edit analysis or estimations on this request
     */
    public boolean canEdit(Request request) {
        if (!canView(request)) return false;
        if (isAdmin()) return true;
        User current = currentUser();
        if (request.getAnalysedBy() == null || isLockExpired(request.getAnalysedAt())) return true;
        return request.getAnalysedBy().getId().equals(current.getId());
    }

    /**
     * Checks the lock before saving analysis or estimation data.
     *
     * @param request parent request
     * @throws AccessDeniedException when the user cannot see the request
     * @throws IllegalStateException when another analyst holds the lock
     */
    public void requireEdit(Request request) {
        requireView(request);
        if (!canEdit(request)) {
            throw new IllegalStateException("Request is locked for analysis by another user");
        }
    }

    /**
     * Validates the lock and renews it. Saving analysis or estimation extends the timeout.
     *
     * @param request parent request
     * @throws AccessDeniedException when the user cannot see the request
     * @throws IllegalStateException when another analyst holds the lock
     */
    public void requireEditAndRenew(Request request) {
        requireEdit(request);
        renewLock(request, currentUser());
    }

    /**
     * Takes the lock when the analyst starts working on a request.
     *
     * @param request request to lock
     * @throws AccessDeniedException when the user cannot see the request
     * @throws IllegalStateException when another analyst holds the lock and it has not expired
     */
    public void lock(Request request) {
        requireView(request);
        User current = currentUser();
        if (!isLockExpired(request.getAnalysedAt()) && request.getAnalysedBy() != null
                && !request.getAnalysedBy().getId().equals(current.getId()) && !isAdmin()) {
            throw new IllegalStateException("Request is already locked for analysis by another user");
        }
        renewLock(request, current);
    }

    /**
     * Releases the edit lock on a request.
     *
     * @param request request to unlock
     * @throws AccessDeniedException when the user cannot see the request or is not the lock owner
     */
    public void unlock(Request request) {
        requireView(request);
        User current = currentUser();
        if (request.getAnalysedBy() != null && !request.getAnalysedBy().getId().equals(current.getId()) && !isAdmin()) {
            throw new AccessDeniedException("Only the lock owner or an administrator can unlock this request");
        }
        request.setAnalysedBy(null);
        request.setAnalysedAt(null);
    }

    /**
     * Sets the user as the current editor and updates the lock timestamp.
     *
     * @param request request to update
     * @param user analyst who holds the lock
     */
    public void renewLock(Request request, User user) {
        request.setAnalysedBy(user);
        request.setAnalysedAt(LocalDateTime.now());
    }

    /**
     * Checks if the edit lock has expired.
     *
     * @param analysedAt timestamp when the lock was taken
     * @return true if there is no lock or LOCK_TIMEOUT has passed
     */
    public boolean isLockExpired(LocalDateTime analysedAt) {
        return analysedAt == null || analysedAt.plus(LOCK_TIMEOUT).isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the logged-in user has the ADMIN role.
     *
     * @return true if the current user is an administrator
     */
    public boolean isAdmin() {
        return currentAuth().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    /**
     * Returns the user from the current HTTP request.
     *
     * @return authenticated user from the JWT
     * @throws AccessDeniedException when there is no valid session
     */
    public User currentUser() {
        return userRepository.findByEmail(currentAuth().getName()).orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    /**
     * Returns the Redmine instance id linked to the user credential, if any.
     *
     * @param user user to look up
     * @return Redmine instance id when the user has saved credentials
     */
    public Optional<Long> redmineInstanceId(User user) {
        return credentialRepository.findRedmineInstanceIdByUserId(user.getId());
    }

    private Authentication currentAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authenticated user not found");
        }
        return auth;
    }

    private boolean ownsManualRequest(Request request, User current) {
        if (request.getOwner() != null && request.getOwner().getId().equals(current.getId())) return true;
        Project project = request.getProject();
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(current.getId());
    }
}
