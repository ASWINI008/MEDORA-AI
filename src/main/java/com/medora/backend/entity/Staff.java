package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // DOCTOR, NURSE, ADMINISTRATOR

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "availability_status", nullable = false)
    private String availabilityStatus; // AVAILABLE, ON_DUTY, OFF_DUTY

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "workload_index")
    private Double workloadIndex; // 0.0 to 100.0

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Staff() {}

    public Staff(String name, String role, Department department, String availabilityStatus, String contactNumber, Double workloadIndex) {
        this.name = name;
        this.role = role;
        this.department = department;
        this.availabilityStatus = availabilityStatus;
        this.contactNumber = contactNumber;
        this.workloadIndex = workloadIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Double getWorkloadIndex() { return workloadIndex; }
    public void setWorkloadIndex(Double workloadIndex) { this.workloadIndex = workloadIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

