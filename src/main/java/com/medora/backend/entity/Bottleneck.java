package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bottlenecks")
public class Bottleneck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "bottleneck_type", nullable = false)
    private String bottleneckType; // HIGH_PATIENT_QUEUE, STAFF_SHORTAGE, BED_SHORTAGE

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "detected_time", nullable = false)
    private LocalDateTime detectedTime = LocalDateTime.now();

    @Column(name = "contributing_factors", length = 500)
    private String contributingFactors;

    @Column(name = "predicted_impact", length = 500)
    private String predictedImpact;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, RESOLVED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Bottleneck() {}

    public Bottleneck(Department department, String bottleneckType, String severity, String contributingFactors, String predictedImpact) {
        this.department = department;
        this.bottleneckType = bottleneckType;
        this.severity = severity;
        this.contributingFactors = contributingFactors;
        this.predictedImpact = predictedImpact;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getBottleneckType() { return bottleneckType; }
    public void setBottleneckType(String bottleneckType) { this.bottleneckType = bottleneckType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public LocalDateTime getDetectedTime() { return detectedTime; }
    public void setDetectedTime(LocalDateTime detectedTime) { this.detectedTime = detectedTime; }
    public String getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(String contributingFactors) { this.contributingFactors = contributingFactors; }
    public String getPredictedImpact() { return predictedImpact; }
    public void setPredictedImpact(String predictedImpact) { this.predictedImpact = predictedImpact; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
