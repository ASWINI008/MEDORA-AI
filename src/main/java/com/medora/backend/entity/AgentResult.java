package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_results")
public class AgentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_name", nullable = false)
    private String agentName; // PATIENT_FLOW_AGENT, STAFF_AGENT, RESOURCE_AGENT, etc.

    @Column(name = "analysis_input", length = 1000)
    private String analysisInput;

    @Column(name = "analysis_output", length = 2000)
    private String analysisOutput;

    @Column(name = "execution_time")
    private Long executionTime; // Milliseconds taken

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AgentResult() {}

    public AgentResult(String agentName, String analysisInput, String analysisOutput, Long executionTime) {
        this.agentName = agentName;
        this.analysisInput = analysisInput;
        this.analysisOutput = analysisOutput;
        this.executionTime = executionTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getAnalysisInput() { return analysisInput; }
    public void setAnalysisInput(String analysisInput) { this.analysisInput = analysisInput; }
    public String getAnalysisOutput() { return analysisOutput; }
    public void setAnalysisOutput(String analysisOutput) { this.analysisOutput = analysisOutput; }
    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
