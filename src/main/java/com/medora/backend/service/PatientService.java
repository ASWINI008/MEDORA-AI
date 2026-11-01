package com.medora.backend.service;

import com.medora.backend.entity.Patient;
import com.medora.backend.entity.PatientFlow;
import com.medora.backend.entity.Resource;
import com.medora.backend.entity.Staff;
import com.medora.backend.entity.Doctor;
import com.medora.backend.repository.PatientFlowRepository;
import com.medora.backend.repository.PatientRepository;
import com.medora.backend.repository.ResourceRepository;
import com.medora.backend.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PatientFlowRepository patientFlowRepository;

    @Autowired
    private StaffRepository staffRepository;

    public List<Patient> getAllPatients(String department, String status, String priority, String search) {
        List<Patient> list = patientRepository.findAll();

        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            list = list.stream()
                    .filter(p -> p.getCurrentDepartment() != null && p.getCurrentDepartment().getName().toLowerCase().contains(department.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (priority != null && !priority.trim().isEmpty() && !"All".equalsIgnoreCase(priority)) {
            list = list.stream()
                    .filter(p -> p.getPriority() != null && p.getPriority().equalsIgnoreCase(priority))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.toLowerCase();
            list = list.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q)) ||
                                 (p.getPatientReference() != null && p.getPatientReference().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return list;
    }

    public Map<String, Object> getPatientSummary() {
        List<Patient> all = patientRepository.findAll();
        long total = all.size();
        long waiting = all.stream().filter(p -> "WAITING".equalsIgnoreCase(p.getStatus()) || "IN_TRIAGE".equalsIgnoreCase(p.getStatus())).count();
        long inTreatment = all.stream().filter(p -> "IN_TREATMENT".equalsIgnoreCase(p.getStatus()) || "IN_CONSULTATION".equalsIgnoreCase(p.getStatus())).count();
        long admitted = all.stream().filter(p -> "ADMITTED".equalsIgnoreCase(p.getStatus())).count();
        long discharged = all.stream().filter(p -> "DISCHARGED".equalsIgnoreCase(p.getStatus())).count();

        LocalDateTime now = LocalDateTime.now();
        List<Long> waitingTimes = all.stream()
                .filter(p -> "WAITING".equalsIgnoreCase(p.getStatus()) || "IN_TRIAGE".equalsIgnoreCase(p.getStatus()))
                .map(p -> p.getArrivalTime() != null ? Duration.between(p.getArrivalTime(), now).toMinutes() : 0L)
                .collect(Collectors.toList());

        double avgWaitMinutes = waitingTimes.isEmpty() ? 0.0 : waitingTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPatients", total);
        summary.put("waiting", waiting);
        summary.put("inTreatment", inTreatment);
        summary.put("admitted", admitted);
        summary.put("discharged", discharged);
        summary.put("averageWaitingTimeMinutes", Math.round(avgWaitMinutes));

        return summary;
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Patient savePatient(Patient patient) {
        if (patient.getPatientReference() == null || patient.getPatientReference().trim().isEmpty()) {
            patient.setPatientReference("PAT-" + (1000 + (int)(Math.random() * 9000)));
        }
        Patient saved = patientRepository.save(patient);

        // Record Initial Patient Flow Entry
        try {
            if (saved.getCurrentDepartment() != null) {
                PatientFlow flow = new PatientFlow(saved, saved.getCurrentDepartment(), saved.getStage() != null ? saved.getStage() : "REGISTRATION", 0, "ACTIVE");
                patientFlowRepository.save(flow);
            }
        } catch (Exception e) {
            // Ignore optional flow logging errors
        }

        return saved;
    }

    public Patient updatePatient(Long id, Patient details) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));

        String oldStatus = existing.getStatus();
        String newStatus = details.getStatus() != null ? details.getStatus() : oldStatus;

        if (details.getName() != null) existing.setName(details.getName());
        if (details.getAge() > 0) existing.setAge(details.getAge());
        if (details.getGender() != null) existing.setGender(details.getGender());
        if (details.getStatus() != null) existing.setStatus(details.getStatus());
        if (details.getStage() != null) existing.setStage(details.getStage());
        if (details.getPriority() != null) existing.setPriority(details.getPriority());
        if (details.getCurrentDepartment() != null) existing.setCurrentDepartment(details.getCurrentDepartment());
        if (details.getAssignedDoctor() != null) existing.setAssignedDoctor(details.getAssignedDoctor());

        // 1. PATIENT -> BED ALLOCATION (ADMITTED)
        if ("ADMITTED".equalsIgnoreCase(newStatus)) {
            if (existing.getAdmissionTime() == null) {
                existing.setAdmissionTime(LocalDateTime.now());
            }

            // If a specific bed was passed in details, use it
            if (details.getAssignedBed() != null) {
                Resource bed = details.getAssignedBed();
                bed.setStatus("OCCUPIED");
                resourceRepository.save(bed);
                existing.setAssignedBed(bed);
            } else if (existing.getAssignedBed() == null && existing.getCurrentDepartment() != null) {
                // Auto-allocate an AVAILABLE bed in patient's department
                Optional<Resource> freeBed = resourceRepository.findAll().stream()
                        .filter(r -> "BED".equalsIgnoreCase(r.getType()) &&
                                     "AVAILABLE".equalsIgnoreCase(r.getStatus()) &&
                                     r.getDepartment() != null &&
                                     r.getDepartment().getId().equals(existing.getCurrentDepartment().getId()))
                        .findFirst();

                if (freeBed.isPresent()) {
                    Resource bed = freeBed.get();
                    bed.setStatus("OCCUPIED");
                    resourceRepository.save(bed);
                    existing.setAssignedBed(bed);
                }
            }
        }

        // 2. PATIENT -> BED RELEASE (DISCHARGED)
        if ("DISCHARGED".equalsIgnoreCase(newStatus)) {
            if (existing.getDischargeTime() == null) {
                existing.setDischargeTime(LocalDateTime.now());
            }

            // Free up assigned bed
            if (existing.getAssignedBed() != null) {
                Resource bed = existing.getAssignedBed();
                bed.setStatus("AVAILABLE");
                resourceRepository.save(bed);
                existing.setAssignedBed(null);
            }
        }

        Patient updated = patientRepository.save(existing);

        // 3. LOG PATIENT FLOW HISTORY ENTRY
        try {
            if (updated.getCurrentDepartment() != null && (details.getStatus() != null || details.getStage() != null)) {
                int waitMins = updated.getArrivalTime() != null ? (int) Duration.between(updated.getArrivalTime(), LocalDateTime.now()).toMinutes() : 0;
                PatientFlow flow = new PatientFlow(updated, updated.getCurrentDepartment(), updated.getStage() != null ? updated.getStage() : updated.getStatus(), waitMins, "DISCHARGED".equalsIgnoreCase(newStatus) ? "COMPLETED" : "ACTIVE");
                patientFlowRepository.save(flow);
            }
        } catch (Exception e) {
            // Ignore optional flow logging errors
        }

        // 4. RECALCULATE ASSIGNED STAFF WORKLOAD
        try {
            if (updated.getAssignedDoctor() != null) {
                Doctor doc = updated.getAssignedDoctor();
                long docPatientCount = patientRepository.findAll().stream()
                        .filter(p -> p.getAssignedDoctor() != null && p.getAssignedDoctor().getId().equals(doc.getId()) && !"DISCHARGED".equalsIgnoreCase(p.getStatus()))
                        .count();

                // Find corresponding Staff record to update workloadIndex
                Optional<Staff> staffOpt = staffRepository.findAll().stream()
                        .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(doc.getName()))
                        .findFirst();

                if (staffOpt.isPresent()) {
                    Staff staff = staffOpt.get();
                    staff.setWorkloadIndex(Math.min(100.0, 30.0 + (docPatientCount * 15.0)));
                    staffRepository.save(staff);
                }
            }
        } catch (Exception e) {
            // Ignore workload update error
        }

        return updated;
    }
}
