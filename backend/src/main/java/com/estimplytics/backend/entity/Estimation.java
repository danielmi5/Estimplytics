package com.estimplytics.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estimations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estimation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "analysis_id", nullable = false)
    private ImpactAnalysis analysis;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "fiability")
    private Integer fiability;

    @Column(name = "hours_planning")
    private Integer hoursPlanning;

    @Column(name = "hours_analysis")
    private Integer hoursAnalysis;

    @Column(name = "hours_development")
    private Integer hoursDevelopment;

    @Column(name = "hours_testing")
    private Integer hoursTesting;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "actual_hours_feedback")
    private Integer actualHoursFeedback;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
