package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    private String specialization;

    private String services;

    @Column(name = "hpr_id")
    private String hprId;

    @Column(name = "professional_type")
    private String professionalType;

    @Column(nullable = false)
    private String availability = "AVAILABLE"; // AVAILABLE, ON_DUTY, OFF_DUTY, ON_CALL

    @Column(name = "consultation_hours")
    private String consultationHours = "09:00 AM - 05:00 PM";

    @Column(name = "experience_years")
    private Integer experienceYears = 5;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Doctor() {}

    public Doctor(String doctorName, Department department, String specialization, String services, String availability, String consultationHours, Integer experienceYears, String contactNumber) {
        this.doctorName = doctorName;
        this.department = department;
        this.specialization = specialization;
        this.services = services;
        this.availability = availability;
        this.consultationHours = consultationHours;
        this.experienceYears = experienceYears;
        this.contactNumber = contactNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getName() { return doctorName; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    public String getHprId() { return hprId; }
    public void setHprId(String hprId) { this.hprId = hprId; }
    public String getProfessionalType() { return professionalType; }
    public void setProfessionalType(String professionalType) { this.professionalType = professionalType; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getConsultationHours() { return consultationHours; }
    public void setConsultationHours(String consultationHours) { this.consultationHours = consultationHours; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
