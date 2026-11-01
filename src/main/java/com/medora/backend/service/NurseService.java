package com.medora.backend.service;

import com.medora.backend.entity.Nurse;
import com.medora.backend.repository.NurseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NurseService {

    @Autowired
    private NurseRepository nurseRepository;

    public List<Nurse> getAllNurses(String department) {
        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            return nurseRepository.findByDepartmentNameContainingIgnoreCase(department);
        }
        return nurseRepository.findAll();
    }

    public Optional<Nurse> getNurseById(Long id) {
        return nurseRepository.findById(id);
    }

    public Map<String, Object> getNurseDashboardMetrics(String department) {
        List<Nurse> nurses = getAllNurses(department);
        long available = nurses.stream().filter(n -> "AVAILABLE".equalsIgnoreCase(n.getAvailabilityStatus()) || "ON_DUTY".equalsIgnoreCase(n.getAvailabilityStatus())).count();
        double avgWorkload = nurses.isEmpty() ? 50.0 : nurses.stream().mapToDouble(n -> n.getWorkloadPercentage() != null ? n.getWorkloadPercentage() : 50.0).average().orElse(50.0);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalNurses", nurses.size());
        metrics.put("availableNurses", available);
        metrics.put("averageWorkloadPercentage", Math.round(avgWorkload));
        metrics.put("currentShift", "Morning Shift (07:00 AM - 03:00 PM)");

        return metrics;
    }

    public Nurse saveNurse(Nurse nurse) {
        return nurseRepository.save(nurse);
    }

    public Nurse updateNurse(Long id, Nurse details) {
        Nurse existing = nurseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nurse not found with ID: " + id));

        if (details.getName() != null) existing.setName(details.getName());
        if (details.getShift() != null) existing.setShift(details.getShift());
        if (details.getAvailabilityStatus() != null) existing.setAvailabilityStatus(details.getAvailabilityStatus());
        if (details.getWorkloadPercentage() != null) existing.setWorkloadPercentage(details.getWorkloadPercentage());
        if (details.getContactNumber() != null) existing.setContactNumber(details.getContactNumber());
        if (details.getStatus() != null) existing.setStatus(details.getStatus());

        return nurseRepository.save(existing);
    }
}
