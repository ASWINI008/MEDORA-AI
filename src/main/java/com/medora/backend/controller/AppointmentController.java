package com.medora.backend.controller;

import com.medora.backend.entity.Appointment;
import com.medora.backend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT', 'DEPARTMENT_MANAGER')")
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getAppointmentMetrics() {
        List<Appointment> all = appointmentService.getAllAppointments();
        long scheduled = all.stream().filter(a -> "SCHEDULED".equalsIgnoreCase(a.getStatus())).count();
        long completed = all.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long cancelled = all.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalAppointments", all.size());
        metrics.put("scheduledCount", scheduled);
        metrics.put("completedCount", completed);
        metrics.put("cancelledCount", cancelled);

        return ResponseEntity.ok(metrics);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'DEPARTMENT_MANAGER', 'PATIENT')")
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.saveAppointment(appointment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment details) {
        return appointmentService.updateAppointment(id, details)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        if (appointmentService.cancelAppointment(id)) {
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully."));
        }
        return ResponseEntity.notFound().build();
    }
}

