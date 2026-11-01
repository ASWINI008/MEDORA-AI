package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bottleneck_id")
    private Bottleneck bottleneck;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "problem_description", nullable = false, length = 500)
    private String problemDescription;

    @Column(name = "recommended_action", nullable = false, length = 500)
    private String recommendedAction;

    @Column(nullable = false)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(length = 1000)
    private String reasoning;

    @Column(name = "generated_by_agent", nullable = false)
    private String generatedByAgent = "RECOMMENDATION_AGENT";

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, IMPLEMENTED, IGNORED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Recommendation() {}

    public Recommendation(Bottleneck bottleneck, Department department, String problemDescription, String recommendedAction, String priority, String reasoning) {
        this.bottleneck = bottleneck;
        this.department = department;
        this.problemDescription = problemDescription;
        this.recommendedAction = recommendedAction;
        this.priority = priority;
        this.reasoning = reasoning;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Bottleneck getBottleneck() { return bottleneck; }
    public void setBottleneck(Bottleneck bottleneck) { this.bottleneck = bottleneck; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public String getGeneratedByAgent() { return generatedByAgent; }
    public void setGeneratedByAgent(String generatedByAgent) { this.generatedByAgent = generatedByAgent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
