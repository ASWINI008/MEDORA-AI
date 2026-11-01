package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ambulance_requests")
public class AmbulanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_reference", unique = true)
    private String requestReference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ambulance_id")
    private Ambulance ambulance;

    @Column(name = "patient_condition")
    private String patientCondition = "CRITICAL";

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "destination_department")
    private String destinationDepartment = "Emergency Department";

    @Column(nullable = false)
    private String status = "REQUESTED"; // REQUESTED, ASSIGNED, DRIVER_ACCEPTED, ON_ROUTE, ARRIVED, COMPLETED, CANCELLED

    private String eta = "10 mins";

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public AmbulanceRequest() {}

    public AmbulanceRequest(String requestReference, Ambulance ambulance, String patientCondition, String pickupAddress, String destinationDepartment, String status, String eta) {
        this.requestReference = requestReference;
        this.ambulance = ambulance;
        this.patientCondition = patientCondition;
        this.pickupAddress = pickupAddress;
        this.destinationDepartment = destinationDepartment;
        this.status = status;
        this.eta = eta;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestReference() { return requestReference; }
    public void setRequestReference(String requestReference) { this.requestReference = requestReference; }
    public Ambulance getAmbulance() { return ambulance; }
    public void setAmbulance(Ambulance ambulance) { this.ambulance = ambulance; }
    public String getPatientCondition() { return patientCondition; }
    public void setPatientCondition(String patientCondition) { this.patientCondition = patientCondition; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getDestinationDepartment() { return destinationDepartment; }
    public void setDestinationDepartment(String destinationDepartment) { this.destinationDepartment = destinationDepartment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
