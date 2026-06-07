package com.estimplytics.backend.mapper;

import com.estimplytics.backend.dto.redmine.RedmineIssueDTO;
import com.estimplytics.backend.entity.RedmineIssueMetadata;
import com.estimplytics.backend.entity.Request;
import com.estimplytics.backend.exception.RedmineMandatoryFieldException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Component
public class RedmineIssueMapper {

    public void updateEntityAndMetadataFromDto(RedmineIssueDTO dto, Request request, RedmineIssueMetadata metadata) {
        metadata.setRedmineId(dto.getId());
        request.setOriginRequestCode("REDMINE-%s".formatted(dto.getId()));

        if (dto.getProject() == null || dto.getProject().getName() == null || dto.getProject().getName().isBlank()) {
            throw new RedmineMandatoryFieldException("Project is required but was null or empty in Redmine issue %s".formatted(dto.getId()));
        }
        metadata.setProjectName(dto.getProject().getName());
        metadata.setProjectId(dto.getProject().getId() != null ? dto.getProject().getId().longValue() : null);

        if (dto.getTracker() == null || dto.getTracker().getName() == null || dto.getTracker().getName().isBlank()) {
            throw new RedmineMandatoryFieldException("Tracker is required but was null or empty in Redmine issue %s".formatted(dto.getId()));
        }
        metadata.setRawTracker(dto.getTracker().getName());

        if (dto.getSubject() == null || dto.getSubject().isBlank()) {
            throw new RedmineMandatoryFieldException("Subject is required but was null or empty in Redmine issue %s".formatted(dto.getId()));
        }

        String statusName = dto.getStatus() != null ? dto.getStatus().getName() : null;
        if (statusName == null || statusName.isBlank()) {
            throw new RedmineMandatoryFieldException("Status is required but was null or empty in Redmine issue %s".formatted(dto.getId()));
        }
        metadata.setRawStatus(statusName);

        if (dto.getPriority() == null || dto.getPriority().getName() == null || dto.getPriority().getName().isBlank()) {
            request.setPriority("Normal");
        } else {
            request.setPriority(dto.getPriority().getName());
        }

        metadata.setAuthorName(dto.getAuthor() != null ? dto.getAuthor().getName() : null);
        metadata.setAssigneeName(dto.getAssignedTo() != null ? dto.getAssignedTo().getName() : null);

        metadata.setRedmineCreatedDate(parseDateTimeSafe(dto.getCreatedOn()));
        metadata.setRedmineUpdatedDate(parseDateTimeSafe(dto.getUpdatedOn()));
        metadata.setRedmineClosedDate(parseDateTimeSafe(dto.getClosedOn()));

        request.setTitle(dto.getSubject());
        request.setDescription(dto.getDescription());
        request.setStatus(mapStatus(statusName));
        request.setDemandType(dto.getTracker().getName());
        request.setStartDate(parseDateSafe(dto.getStartDate()));
        request.setEndDate(parseDateSafe(dto.getDueDate()));
        request.setDoneRatio(dto.getDoneRatio());
        request.setEstimatedHours(dto.getEstimatedHours());
        request.setSpentHours(dto.getSpentHours());
    }

    private String mapStatus(String redmineStatus) {
        return switch (redmineStatus.toLowerCase()) {
            case "new", "open" -> "OPEN";
            case "in progress", "feedback" -> "IN_PROGRESS";
            case "closed", "resolved", "rejected" -> "CLOSED";
            default -> "OPEN";
        };
    }

    private LocalDate parseDateSafe(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTimeSafe(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateTime).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
