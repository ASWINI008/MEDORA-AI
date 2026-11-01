package com.medora.backend.controller;

import com.medora.backend.entity.Patient;
import com.medora.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient-flow")
public class PatientFlowController {

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public ResponseEntity<?> getPatientFlow(@RequestParam(required = false) String department) {
        List<Patient> allPatients = patientRepository.findAll();
        
        List<Patient> filteredPatients = (department == null || department.equalsIgnoreCase("All"))
                ? allPatients
                : allPatients.stream()
                        .filter(p -> p.getCurrentDepartment() != null && p.getCurrentDepartment().getName().equalsIgnoreCase(department))
                        .collect(Collectors.toList());

        long waitingCount = filteredPatients.stream().filter(p -> "WAITING".equalsIgnoreCase(p.getStatus())).count();
        long inTriageCount = filteredPatients.stream().filter(p -> "WAITING".equalsIgnoreCase(p.getStatus()) || "IN TRIAGE".equalsIgnoreCase(p.getStatus()) || "Triage Queue".equalsIgnoreCase(p.getStage())).count();
        long admittedCount = filteredPatients.stream().filter(p -> "ADMITTED".equalsIgnoreCase(p.getStatus()) || "IN TREATMENT".equalsIgnoreCase(p.getStatus())).count();
        long dischargedCount = filteredPatients.stream().filter(p -> "DISCHARGED".equalsIgnoreCase(p.getStatus())).count();

        // Dynamically compute average wait minutes for waiting patients
        long totalWaitMinutes = 0;
        int waitPatientCount = 0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Patient p : filteredPatients) {
            if (p.getArrivalTime() != null && ("WAITING".equalsIgnoreCase(p.getStatus()) || "IN TRIAGE".equalsIgnoreCase(p.getStatus()))) {
                long mins = java.time.Duration.between(p.getArrivalTime(), now).toMinutes();
                totalWaitMinutes += Math.max(0, mins);
                waitPatientCount++;
            }
        }

        int avgWaitMinutes = waitPatientCount > 0 ? (int) (totalWaitMinutes / waitPatientCount) : (waitingCount > 0 ? (int)(waitingCount * 12 + 15) : 18);
        int queueDensityPercent = filteredPatients.isEmpty() ? 0 : (int) Math.min(100, Math.round((inTriageCount * 100.0) / filteredPatients.size()));

        Map<String, Object> flowData = new HashMap<>();
        flowData.put("totalTracked", filteredPatients.size());
        flowData.put("waiting", waitingCount);
        flowData.put("inTriageCount", inTriageCount);
        flowData.put("triageCount", inTriageCount);
        flowData.put("admitted", admittedCount);
        flowData.put("discharged", dischargedCount);
        flowData.put("averageWaitMinutes", avgWaitMinutes);
        flowData.put("queueDensityPercent", queueDensityPercent);
        flowData.put("patients", filteredPatients);

        return ResponseEntity.ok(flowData);
    }
}

