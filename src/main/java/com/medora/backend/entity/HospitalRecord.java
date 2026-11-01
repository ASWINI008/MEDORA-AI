package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing one row from the hospital analytics CSV dataset.
 * Patient_ID from the CSV is stored as patientExternalId — it is a dataset
 * identifier only and is NOT used for ML predictions.
 */
@Entity
@Table(name = "hospital_records")
public class HospitalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dataset Patient_ID — used only for deduplication, not predictions. */
    @Column(name = "patient_external_id", unique = true)
    private Integer patientExternalId;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "medical_condition")
    private String condition;

    @Column(name = "medical_procedure")
    private String procedure;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "length_of_stay")
    private Integer lengthOfStay;

    /** 'Yes' or 'No' */
    @Column(name = "readmission")
    private String readmission;

    @Column(name = "outcome")
    private String outcome;

    @Column(name = "satisfaction")
    private Integer satisfaction;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    public HospitalRecord() {}

    public HospitalRecord(Integer patientExternalId, Integer age, String gender,
                          String condition, String procedure, Double cost,
                          Integer lengthOfStay, String readmission, String outcome,
                          Integer satisfaction, LocalDateTime importedAt) {
        this.patientExternalId = patientExternalId;
        this.age = age;
        this.gender = gender;
        this.condition = condition;
        this.procedure = procedure;
        this.cost = cost;
        this.lengthOfStay = lengthOfStay;
        this.readmission = readmission;
        this.outcome = outcome;
        this.satisfaction = satisfaction;
        this.importedAt = importedAt;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPatientExternalId() { return patientExternalId; }
    public void setPatientExternalId(Integer patientExternalId) { this.patientExternalId = patientExternalId; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getProcedure() { return procedure; }
    public void setProcedure(String procedure) { this.procedure = procedure; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public Integer getLengthOfStay() { return lengthOfStay; }
    public void setLengthOfStay(Integer lengthOfStay) { this.lengthOfStay = lengthOfStay; }

    public String getReadmission() { return readmission; }
    public void setReadmission(String readmission) { this.readmission = readmission; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Integer getSatisfaction() { return satisfaction; }
    public void setSatisfaction(Integer satisfaction) { this.satisfaction = satisfaction; }

    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
}
