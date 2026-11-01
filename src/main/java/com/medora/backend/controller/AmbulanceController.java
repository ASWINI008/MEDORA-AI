package com.medora.backend.controller;

import com.medora.backend.entity.Ambulance;
import com.medora.backend.entity.AmbulanceRequest;
import com.medora.backend.service.AmbulanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {

    @Autowired
    private AmbulanceService ambulanceService;

    @GetMapping
    public ResponseEntity<List<Ambulance>> getAllAmbulances(@RequestParam(required = false, defaultValue = "All") String status) {
        return ResponseEntity.ok(ambulanceService.getAllAmbulances(status));
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getEmergencyOverview() {
        return ResponseEntity.ok(ambulanceService.getEmergencyAmbulanceOverview());
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AmbulanceRequest>> getAllRequests(
            @RequestParam(required = false, defaultValue = "All") String status,
            @RequestParam(required = false, defaultValue = "All") String department) {
        return ResponseEntity.ok(ambulanceService.getAllRequests(status, department));
    }

    @PostMapping
    public ResponseEntity<Ambulance> createAmbulance(@RequestBody Ambulance ambulance) {
        return ResponseEntity.ok(ambulanceService.saveAmbulance(ambulance));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ambulance> updateAmbulanceStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String eta = payload.get("eta");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(ambulanceService.updateAmbulanceStatus(id, status, eta));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/requests")
    public ResponseEntity<AmbulanceRequest> createRequest(@RequestBody AmbulanceRequest request) {
        return ResponseEntity.ok(ambulanceService.createAmbulanceRequest(request));
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<AmbulanceRequest> updateRequestStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(ambulanceService.updateRequestStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
