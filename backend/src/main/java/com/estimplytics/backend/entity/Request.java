package com.estimplytics.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(length = 20, nullable = false)
    private String priority;

    @Column(name = "demand_type", length = 50)
    private String demandType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "done_ratio")
    private Integer doneRatio;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "spent_hours")
    private Double spentHours;

    @Column(name = "origin_request_code", length = 100)
    private String originRequestCode;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysed_by_id")
    private User analysedBy;

    @Column(name = "analysed_at")
    private LocalDateTime analysedAt;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ImpactAnalysis impactAnalysis;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RedmineIssueMetadata redmineMetadata;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
