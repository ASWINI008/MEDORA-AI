package com.medora.backend.controller;

import com.medora.backend.entity.Nurse;
import com.medora.backend.service.NurseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nurses")
public class NurseController {

    @Autowired
    private NurseService nurseService;

    @GetMapping
    public ResponseEntity<List<Nurse>> getAllNurses(@RequestParam(required = false, defaultValue = "All") String department) {
        return ResponseEntity.ok(nurseService.getAllNurses(department));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getNurseMetrics(@RequestParam(required = false, defaultValue = "All") String department) {
        return ResponseEntity.ok(nurseService.getNurseDashboardMetrics(department));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nurse> getNurseById(@PathVariable Long id) {
        return nurseService.getNurseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Nurse> createNurse(@RequestBody Nurse nurse) {
        return ResponseEntity.ok(nurseService.saveNurse(nurse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nurse> updateNurse(@PathVariable Long id, @RequestBody Nurse nurse) {
        try {
            return ResponseEntity.ok(nurseService.updateNurse(id, nurse));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
