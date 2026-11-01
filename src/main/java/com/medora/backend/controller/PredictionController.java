package com.medora.backend.controller;

import com.medora.backend.entity.*;
import com.medora.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AmbulanceRequestRepository ambulanceRequestRepository;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<List<Prediction>> getAllPredictions() {
        return ResponseEntity.ok(predictionRepository.findAll());
    }

    @GetMapping("/department")
    public ResponseEntity<?> getDepartmentPrediction(@RequestParam(value = "department", defaultValue = "Emergency Department") String department) {
        // Collect live operational features from JPA Repositories
        List<Patient> allPatients = patientRepository.findAll();
        List<Staff> allStaff = staffRepository.findAll();
        List<Resource> allResources = resourceRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();

        long waitingQueue = allPatients.stream().filter(p -> "WAITING".equalsIgnoreCase(p.getStatus())).count();
        long availableDocs = allStaff.stream().filter(s -> "DOCTOR".equalsIgnoreCase(s.getRole()) && ("ON_DUTY".equalsIgnoreCase(s.getAvailabilityStatus()) || "AVAILABLE".equalsIgnoreCase(s.getAvailabilityStatus()))).count();
        long availableNurses = allStaff.stream().filter(s -> "NURSE".equalsIgnoreCase(s.getRole()) && ("ON_DUTY".equalsIgnoreCase(s.getAvailabilityStatus()) || "AVAILABLE".equalsIgnoreCase(s.getAvailabilityStatus()))).count();
        long totalBeds = allResources.stream().filter(r -> "BED".equalsIgnoreCase(r.getType())).count();
        long freeBeds = allResources.stream().filter(r -> "BED".equalsIgnoreCase(r.getType()) && "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
        long scheduledAppts = allAppointments.stream().filter(a -> "SCHEDULED".equalsIgnoreCase(a.getStatus())).count();
        long cancelledAppts = allAppointments.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();

        double bedOccupancy = totalBeds > 0 ? Math.round(((double)(totalBeds - freeBeds) / totalBeds) * 100.0) : 75.0;
        double avgWorkload = allStaff.stream().mapToDouble(s -> s.getWorkloadIndex() != null ? s.getWorkloadIndex() : 65.0).average().orElse(70.0);

        long ambulanceCount = 0;
        try {
            ambulanceCount = ambulanceRequestRepository.findAll().stream()
                    .filter(r -> List.of("REQUESTED", "ASSIGNED", "DRIVER_ACCEPTED", "ON_ROUTE").contains(r.getStatus().toUpperCase()))
                    .count();
        } catch (Exception e) {
            ambulanceCount = 2;
        }

        Map<String, Object> featurePayload = new HashMap<>();
        featurePayload.put("department", department);
        featurePayload.put("patient_arrival_rate", Math.max(12, (int) (waitingQueue * 2 + 10)));
        featurePayload.put("queue_length", (int) waitingQueue);
        featurePayload.put("average_waiting_time", Math.max(15.0, waitingQueue * 8.0 + 10.0));
        featurePayload.put("available_doctors", (int) availableDocs);
        featurePayload.put("available_nurses", (int) availableNurses);
        featurePayload.put("staff_workload", avgWorkload);
        featurePayload.put("available_beds", (int) freeBeds);
        featurePayload.put("bed_occupancy", bedOccupancy);
        featurePayload.put("resource_utilization", bedOccupancy);
        featurePayload.put("appointment_count", (int) scheduledAppts);
        featurePayload.put("cancellation_count", (int) cancelledAppts);
        featurePayload.put("department_workload", avgWorkload);
        featurePayload.put("ambulance_arrivals", (int) ambulanceCount);

        try {
            String targetUrl = aiServiceUrl + "/api/ai/predict";
            ResponseEntity<Map> response = restTemplate.postForEntity(targetUrl, featurePayload, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = new HashMap<>(response.getBody());
                body.put("actualQueueLength", waitingQueue);
                body.put("actualAvailableBeds", freeBeds);
                body.put("actualAvailableStaff", availableDocs + availableNurses);
                body.put("actualAppointments", scheduledAppts);
                return ResponseEntity.ok(body);
            }
        } catch (Exception e) {
            // ML Service offline or model files missing -> Return UNAVAILABLE_PENDING status cleanly
        }

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("department", department);
        fallback.put("status", "UNAVAILABLE");
        fallback.put("predictionStatus", "UNAVAILABLE_PENDING");
        fallback.put("message", "Python ML service unavailable or models pending training at " + aiServiceUrl);
        fallback.put("modelVersion", "UNTRAINED");
        fallback.put("actualQueueLength", waitingQueue);
        fallback.put("actualAvailableBeds", freeBeds);
        fallback.put("actualAvailableStaff", availableDocs + availableNurses);
        fallback.put("actualAppointments", scheduledAppts);

        return ResponseEntity.ok(fallback);
    }

    @PostMapping
    public ResponseEntity<?> createPrediction(@RequestBody Map<String, Object> requestPayload) {
        try {
            String targetUrl = aiServiceUrl + "/api/ai/predict";
            ResponseEntity<Map> response = restTemplate.postForEntity(targetUrl, requestPayload, Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "UNAVAILABLE",
                "predictionStatus", "UNAVAILABLE_PENDING",
                "message", "Could not connect to Python AI Service at " + aiServiceUrl + ". Ensure FastAPI is running."
            ));
        }
    }
}
