package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_reference", unique = true)
    private String patientReference;

    @Column(name = "abha_number", unique = true)
    private String abhaNumber;

    @Column(name = "abha_address", unique = true)
    private String abhaAddress;

    @Column(nullable = false)
    private String name; // Generic pseudonym / synthetic ID display

    private int age;
    private String gender;

    @Column(nullable = false)
    private String status = "WAITING"; // WAITING, IN_TRIAGE, IN_CONSULTATION, IN_TREATMENT, ADMITTED, DISCHARGED

    private String stage = "REGISTRATION"; // REGISTRATION, TRIAGE, CONSULTATION, TREATMENT, ADMISSION, DISCHARGE
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_department_id")
    private Department currentDepartment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_bed_id")
    private Resource assignedBed;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime = LocalDateTime.now();

    @Column(name = "admission_time")
    private LocalDateTime admissionTime;

    @Column(name = "discharge_time")
    private LocalDateTime dischargeTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Patient() {}

    public Patient(String name, int age, String gender, String status, Department currentDepartment) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.currentDepartment = currentDepartment;
        this.patientReference = "PAT-" + (1000 + (int)(Math.random() * 9000));
    }

    public Patient(String name, int age, String gender, String status, Department currentDepartment, String stage, String priority) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.currentDepartment = currentDepartment;
        this.stage = stage;
        this.priority = priority;
        this.patientReference = "PAT-" + (1000 + (int)(Math.random() * 9000));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPatientReference() { return patientReference; }
    public void setPatientReference(String patientReference) { this.patientReference = patientReference; }
    public String getAbhaNumber() { return abhaNumber; }
    public void setAbhaNumber(String abhaNumber) { this.abhaNumber = abhaNumber; }
    public String getAbhaAddress() { return abhaAddress; }
    public void setAbhaAddress(String abhaAddress) { this.abhaAddress = abhaAddress; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Department getCurrentDepartment() { return currentDepartment; }
    public void setCurrentDepartment(Department currentDepartment) { this.currentDepartment = currentDepartment; }
    public Doctor getAssignedDoctor() { return assignedDoctor; }
    public void setAssignedDoctor(Doctor assignedDoctor) { this.assignedDoctor = assignedDoctor; }
    public Resource getAssignedBed() { return assignedBed; }
    public void setAssignedBed(Resource assignedBed) { this.assignedBed = assignedBed; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public LocalDateTime getAdmissionTime() { return admissionTime; }
    public void setAdmissionTime(LocalDateTime admissionTime) { this.admissionTime = admissionTime; }
    public LocalDateTime getDischargeTime() { return dischargeTime; }
    public void setDischargeTime(LocalDateTime dischargeTime) { this.dischargeTime = dischargeTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
