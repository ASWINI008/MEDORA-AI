package com.medora.backend.controller;

import com.medora.backend.entity.Staff;
import com.medora.backend.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff(@RequestParam(required = false) String department) {
        return ResponseEntity.ok(staffService.getStaffByDepartment(department));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getStaffMetrics() {
        List<Staff> allStaff = staffService.getAllStaff();
        long doctorsOnDuty = allStaff.stream().filter(s -> "DOCTOR".equalsIgnoreCase(s.getRole()) && ("ON_DUTY".equalsIgnoreCase(s.getAvailabilityStatus()) || "AVAILABLE".equalsIgnoreCase(s.getAvailabilityStatus()))).count();
        long nursesOnDuty = allStaff.stream().filter(s -> "NURSE".equalsIgnoreCase(s.getRole()) && ("ON_DUTY".equalsIgnoreCase(s.getAvailabilityStatus()) || "AVAILABLE".equalsIgnoreCase(s.getAvailabilityStatus()))).count();
        double avgWorkload = allStaff.stream().mapToDouble(s -> s.getWorkloadIndex() != null ? s.getWorkloadIndex() : 0.0).average().orElse(65.0);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("doctorsOnDuty", doctorsOnDuty);
        metrics.put("nursesOnDuty", nursesOnDuty);
        metrics.put("avgWorkloadIndex", Math.round(avgWorkload));
        metrics.put("totalStaff", allStaff.size());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        return ResponseEntity.ok(staffService.saveStaff(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(@PathVariable Long id, @RequestBody Staff details) {
        return staffService.updateStaff(id, details)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

