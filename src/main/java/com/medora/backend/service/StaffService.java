package com.medora.backend.service;

import com.medora.backend.entity.Staff;
import com.medora.backend.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Staff> getStaffByDepartment(String deptName) {
        if (deptName == null || deptName.equalsIgnoreCase("All")) {
            return staffRepository.findAll();
        }
        return staffRepository.findAll().stream()
                .filter(s -> s.getDepartment() != null &&
                        s.getDepartment().getName().equalsIgnoreCase(deptName))
                .collect(Collectors.toList());
    }

    public Staff saveStaff(Staff staff) {
        Staff saved = staffRepository.save(staff);
        eventPublisherService.publishEvent("staff-availability", Map.of(
                "staffId", saved.getId(),
                "name", saved.getName(),
                "role", saved.getRole(),
                "status", saved.getAvailabilityStatus()
        ));
        return saved;
    }

    public Optional<Staff> updateStaff(Long id, Staff details) {
        return staffRepository.findById(id).map(s -> {
            if (details.getName() != null) s.setName(details.getName());
            if (details.getRole() != null) s.setRole(details.getRole());
            if (details.getAvailabilityStatus() != null) s.setAvailabilityStatus(details.getAvailabilityStatus());
            if (details.getWorkloadIndex() != null) s.setWorkloadIndex(details.getWorkloadIndex());
            Staff updated = staffRepository.save(s);

            eventPublisherService.publishEvent("staff-update", Map.of(
                    "staffId", updated.getId(),
                    "availability", updated.getAvailabilityStatus(),
                    "workload", updated.getWorkloadIndex()
            ));

            return updated;
        });
    }
}
