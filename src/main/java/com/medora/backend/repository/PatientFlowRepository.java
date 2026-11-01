package com.medora.backend.repository;

import com.medora.backend.entity.PatientFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientFlowRepository extends JpaRepository<PatientFlow, Long> {
}
