package com.medora.backend.repository;

import com.medora.backend.entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {
    List<Nurse> findByDepartmentNameContainingIgnoreCase(String departmentName);
    List<Nurse> findByAvailabilityStatus(String availabilityStatus);
}
