package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "prediction_type", nullable = false)
    private String predictionType; // WAITING_TIME, CONGESTION, RESOURCE_DEMAND

    @Column(name = "input_factors", length = 1000)
    private String inputFactors; // Stringified inputs parameters JSON

    @Column(name = "predicted_value", nullable = false)
    private String predictedValue;

    @Column(name = "prediction_time", nullable = false)
    private LocalDateTime predictionTime = LocalDateTime.now();

    @Column(name = "model_used")
    private String modelUsed; // RANDOM_FOREST, XGBOOST, K_MEANS

    @Column(name = "model_status", nullable = false)
    private String modelStatus; // TRAINED, DEMO_SAMPLE

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Prediction() {}

    public Prediction(Department department, String predictionType, String inputFactors, String predictedValue, String modelUsed, String modelStatus) {
        this.department = department;
        this.predictionType = predictionType;
        this.inputFactors = inputFactors;
        this.predictedValue = predictedValue;
        this.modelUsed = modelUsed;
        this.modelStatus = modelStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getPredictionType() { return predictionType; }
    public void setPredictionType(String predictionType) { this.predictionType = predictionType; }
    public String getInputFactors() { return inputFactors; }
    public void setInputFactors(String inputFactors) { this.inputFactors = inputFactors; }
    public String getPredictedValue() { return predictedValue; }
    public void setPredictedValue(String predictedValue) { this.predictedValue = predictedValue; }
    public LocalDateTime getPredictionTime() { return predictionTime; }
    public void setPredictionTime(LocalDateTime predictionTime) { this.predictionTime = predictionTime; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public String getModelStatus() { return modelStatus; }
    public void setModelStatus(String modelStatus) { this.modelStatus = modelStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
