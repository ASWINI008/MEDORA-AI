package com.medora.backend.repository;

import com.medora.backend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartmentNameContainingIgnoreCase(String departmentName);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    List<Doctor> findByAvailability(String availability);
}
