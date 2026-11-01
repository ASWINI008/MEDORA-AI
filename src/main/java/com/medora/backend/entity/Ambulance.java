package com.medora.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, ASSIGNED, ON_ROUTE, AT_HOSPITAL, MAINTENANCE

    @Column(name = "current_assignment")
    private String currentAssignment;

    @Column(name = "pickup_location")
    private String pickupLocation;

    private String destination = "Emergency Department";

    @Column(name = "estimated_arrival")
    private String estimatedArrival = "10 mins";

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Ambulance() {}

    public Ambulance(String vehicleNumber, String driverName, String status, String currentAssignment, String pickupLocation, String destination, String estimatedArrival) {
        this.vehicleNumber = vehicleNumber;
        this.driverName = driverName;
        this.status = status;
        this.currentAssignment = currentAssignment;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.estimatedArrival = estimatedArrival;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentAssignment() { return currentAssignment; }
    public void setCurrentAssignment(String currentAssignment) { this.currentAssignment = currentAssignment; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
