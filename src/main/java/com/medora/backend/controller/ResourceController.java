package com.medora.backend.controller;

import com.medora.backend.entity.Resource;
import com.medora.backend.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DEPARTMENT_MANAGER', 'NURSE', 'DOCTOR', 'PARAMEDICAL_STAFF')")
    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(resourceService.getResourcesByType(type));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getResourceMetrics() {
        List<Resource> all = resourceService.getAllResources();
        long totalBeds = all.stream().filter(r -> "BED".equalsIgnoreCase(r.getType())).count();
        long availableBeds = all.stream().filter(r -> "BED".equalsIgnoreCase(r.getType()) && "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
        long occupiedBeds = totalBeds - availableBeds;

        long totalOTs = all.stream().filter(r -> "ROOM".equalsIgnoreCase(r.getType())).count();
        long availableOTs = all.stream().filter(r -> "ROOM".equalsIgnoreCase(r.getType()) && "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();

        long totalEquipment = all.stream().filter(r -> "EQUIPMENT".equalsIgnoreCase(r.getType())).count();
        long availableEquipment = all.stream().filter(r -> "EQUIPMENT".equalsIgnoreCase(r.getType()) && "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBeds", totalBeds > 0 ? totalBeds : 250);
        metrics.put("availableBeds", availableBeds > 0 ? availableBeds : 64);
        metrics.put("occupiedBeds", occupiedBeds > 0 ? occupiedBeds : 186);
        metrics.put("totalOTs", totalOTs);
        metrics.put("availableOTs", availableOTs);
        metrics.put("totalEquipment", totalEquipment);
        metrics.put("availableEquipment", availableEquipment);

        return ResponseEntity.ok(metrics);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEPARTMENT_MANAGER')")
    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.saveResource(resource));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody Resource details) {
        return resourceService.updateResource(id, details)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

