package com.medora.backend.service;

import com.medora.backend.entity.Ambulance;
import com.medora.backend.entity.AmbulanceRequest;
import com.medora.backend.repository.AmbulanceRepository;
import com.medora.backend.repository.AmbulanceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AmbulanceService {

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private AmbulanceRequestRepository requestRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Ambulance> getAllAmbulances(String status) {
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            return ambulanceRepository.findByStatus(status.toUpperCase());
        }
        return ambulanceRepository.findAll();
    }

    public List<AmbulanceRequest> getAllRequests(String status, String department) {
        List<AmbulanceRequest> list = requestRepository.findAll();
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            list = list.stream().filter(r -> r.getStatus() != null && r.getStatus().equalsIgnoreCase(status)).toList();
        }
        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            list = list.stream().filter(r -> r.getDestinationDepartment() != null && r.getDestinationDepartment().toLowerCase().contains(department.toLowerCase())).toList();
        }
        return list;
    }

    public Map<String, Object> getEmergencyAmbulanceOverview() {
        List<Ambulance> ambulances = ambulanceRepository.findAll();
        List<AmbulanceRequest> requests = requestRepository.findAll();

        long available = ambulances.stream().filter(a -> "AVAILABLE".equalsIgnoreCase(a.getStatus())).count();
        long onRoute = ambulances.stream().filter(a -> "ON_ROUTE".equalsIgnoreCase(a.getStatus()) || "ASSIGNED".equalsIgnoreCase(a.getStatus())).count();
        long atHospital = ambulances.stream().filter(a -> "AT_HOSPITAL".equalsIgnoreCase(a.getStatus())).count();
        long incomingCount = requests.stream().filter(r -> List.of("REQUESTED", "ASSIGNED", "DRIVER_ACCEPTED", "ON_ROUTE").contains(r.getStatus().toUpperCase())).count();

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalAmbulances", ambulances.size());
        overview.put("availableAmbulances", available);
        overview.put("inTransitAmbulances", onRoute);
        overview.put("atHospitalAmbulances", atHospital);
        overview.put("incomingEmergencyCount", incomingCount);

        return overview;
    }

    public Ambulance saveAmbulance(Ambulance ambulance) {
        return ambulanceRepository.save(ambulance);
    }

    public Ambulance updateAmbulanceStatus(Long id, String status, String eta) {
        Ambulance amb = ambulanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ambulance unit not found with ID: " + id));

        amb.setStatus(status.toUpperCase());
        if (eta != null && !eta.trim().isEmpty()) {
            amb.setEstimatedArrival(eta);
        }
        return ambulanceRepository.save(amb);
    }

    public AmbulanceRequest createAmbulanceRequest(AmbulanceRequest request) {
        if (request.getRequestReference() == null || request.getRequestReference().trim().isEmpty()) {
            request.setRequestReference("AMB-REQ-" + (1000 + (int)(Math.random() * 9000)));
        }
        if (request.getStatus() == null) {
            request.setStatus("REQUESTED");
        }
        AmbulanceRequest saved = requestRepository.save(request);

        notificationService.createNotification(
                "Incoming Emergency Dispatch Alert",
                "Pre-arrival emergency dispatch #" + saved.getRequestReference() + " registered for " + saved.getDestinationDepartment() + " (ETA: " + saved.getEta() + ")",
                "CRITICAL",
                "ALL",
                "EMERGENCY"
        );

        return saved;
    }

    public AmbulanceRequest updateRequestStatus(Long id, String status) {
        AmbulanceRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ambulance request not found with ID: " + id));

        String normStatus = status.toUpperCase();
        req.setStatus(normStatus);

        if ("COMPLETED".equalsIgnoreCase(normStatus)) {
            req.setCompletedAt(LocalDateTime.now());
            if (req.getAmbulance() != null) {
                Ambulance amb = req.getAmbulance();
                amb.setStatus("AT_HOSPITAL");
                ambulanceRepository.save(amb);
            }
        } else if ("ON_ROUTE".equalsIgnoreCase(normStatus) && req.getAmbulance() != null) {
            Ambulance amb = req.getAmbulance();
            amb.setStatus("ON_ROUTE");
            ambulanceRepository.save(amb);
        }

        return requestRepository.save(req);
    }
}
