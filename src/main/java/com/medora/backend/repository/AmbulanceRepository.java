package com.medora.backend.repository;

import com.medora.backend.entity.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    Optional<Ambulance> findByVehicleNumber(String vehicleNumber);
    List<Ambulance> findByStatus(String status);
}
