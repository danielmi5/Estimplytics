package com.estimplytics.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "impact_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "request_id", unique = true, nullable = false)
    private Request request;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User analyst;

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Estimation> estimations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentAnalysis> componentAnalyses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImpactAnalysisHistory> histories = new ArrayList<>();

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(length = 20, nullable = false)
    private String complexity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> documentData;

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
