package com.estimplytics.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "redmine_issue_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedmineIssueMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true, nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redmine_instance_id", nullable = false)
    private RedmineInstance redmineInstance;

    @Column(name = "redmine_id", nullable = false)
    private Integer redmineId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", length = 100)
    private String projectName;

    @Column(name = "raw_tracker", length = 100)
    private String rawTracker;

    @Column(name = "raw_status", length = 100)
    private String rawStatus;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "assignee_name", length = 100)
    private String assigneeName;

    @Column(name = "redmine_created_date")
    private LocalDateTime redmineCreatedDate;

    @Column(name = "redmine_updated_date")
    private LocalDateTime redmineUpdatedDate;

    @Column(name = "redmine_closed_date")
    private LocalDateTime redmineClosedDate;
}
