package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nurses")
public class Nurse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private String shift = "Morning Shift (07:00 AM - 03:00 PM)";

    @Column(name = "availability_status", nullable = false)
    private String availabilityStatus = "ON_DUTY"; // AVAILABLE, ON_DUTY, OFF_DUTY

    @Column(name = "workload_percentage")
    private Double workloadPercentage = 50.0;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Nurse() {}

    public Nurse(String name, Department department, String shift, String availabilityStatus, Double workloadPercentage, String contactNumber) {
        this.name = name;
        this.department = department;
        this.shift = shift;
        this.availabilityStatus = availabilityStatus;
        this.workloadPercentage = workloadPercentage;
        this.contactNumber = contactNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Double getWorkloadPercentage() { return workloadPercentage; }
    public void setWorkloadPercentage(Double workloadPercentage) { this.workloadPercentage = workloadPercentage; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
