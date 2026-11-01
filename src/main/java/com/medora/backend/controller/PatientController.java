package com.medora.backend.controller;

import com.medora.backend.entity.Patient;
import com.medora.backend.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.medora.backend.util.SecurityUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private SecurityUtils securityUtils;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT', 'PARAMEDICAL_STAFF', 'DEPARTMENT_MANAGER')")
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients(
            @RequestParam(required = false, defaultValue = "All") String department,
            @RequestParam(required = false, defaultValue = "All") String status,
            @RequestParam(required = false, defaultValue = "All") String priority,
            @RequestParam(required = false) String search) {
        
        securityUtils.validateDepartmentAccess(department);
        return ResponseEntity.ok(patientService.getAllPatients(department, status, priority, search));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPatientSummary() {
        return ResponseEntity.ok(patientService.getPatientSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'DEPARTMENT_MANAGER')")
    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        if (patient.getCurrentDepartment() != null) {
            securityUtils.validateDepartmentAccess(patient.getCurrentDepartment().getName());
        }
        return ResponseEntity.ok(patientService.savePatient(patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            return ResponseEntity.ok(patientService.updatePatient(id, patient));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
