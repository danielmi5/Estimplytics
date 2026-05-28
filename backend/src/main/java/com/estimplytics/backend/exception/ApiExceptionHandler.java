package com.estimplytics.backend.exception;

import com.estimplytics.backend.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Invalid parameter"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "User not found"));
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleRequestNotFoundException(RequestNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Request not found"));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleProjectNotFoundException(ProjectNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Project not found"));
    }

    @ExceptionHandler(ImpactAnalysisNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleImpactAnalysisNotFoundException(ImpactAnalysisNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "ImpactAnalysis not found"));
    }

    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleComponentNotFoundException(ComponentNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Component not found"));
    }

    @ExceptionHandler(ComponentAnalysisNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleComponentAnalysisNotFoundException(ComponentAnalysisNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "ComponentAnalysis not found"));
    }

    @ExceptionHandler(EstimationNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEstimationNotFoundException(EstimationNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Estimation not found"));
    }

    @ExceptionHandler(EstimationHistoryNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEstimationHistoryNotFoundException(EstimationHistoryNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "EstimationHistory not found"));
    }

    @ExceptionHandler(ImpactAnalysisHistoryNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleImpactAnalysisHistoryNotFoundException(ImpactAnalysisHistoryNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "ImpactAnalysisHistory not found"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errors = e.getBindingResult()
                .getAllErrors()
                .stream().map(error -> error.getDefaultMessage()).toList();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, errors.toString()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Access denied"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDTO> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Invalid credentials"));
    }

    @ExceptionHandler(RedmineIntegrationException.class)
    public ResponseEntity<ApiErrorDTO> handleRedmineIntegrationException(RedmineIntegrationException e, HttpServletRequest request) {
        HttpStatus status = determineStatusFromErrorType(e.getErrorType());
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Redmine integration error"));
    }

    @ExceptionHandler(RedmineCredentialNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleRedmineCredentialNotFoundException(RedmineCredentialNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Redmine credential not found"));
    }

    @ExceptionHandler(RedmineInstanceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleRedmineInstanceNotFoundException(RedmineInstanceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createErrorBody(status, request, e, "Redmine instance not found"));
    }

    private HttpStatus determineStatusFromErrorType(RedmineIntegrationException.ErrorType errorType) {
        return switch (errorType) {
            case INVALID_CREDENTIALS -> HttpStatus.SERVICE_UNAVAILABLE;
            case NETWORK_ERROR, TIMEOUT -> HttpStatus.BAD_GATEWAY;
            case INVALID_PAYLOAD -> HttpStatus.BAD_GATEWAY;
        };
    }

    private ApiErrorDTO createErrorBody(HttpStatus status, HttpServletRequest request, Exception e, String desc) {
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = "The provided data is not valid";
        } else {
            message = e.getMessage();
        }

        return ApiErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .stateNum(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .description(desc)
                .path(request.getRequestURI())
                .build();
    }
}