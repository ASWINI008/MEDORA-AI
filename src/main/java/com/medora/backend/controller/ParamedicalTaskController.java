package com.medora.backend.controller;

import com.medora.backend.entity.ParamedicalTask;
import com.medora.backend.service.ParamedicalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paramedical-tasks")
public class ParamedicalTaskController {

    @Autowired
    private ParamedicalTaskService taskService;

    @GetMapping
    public ResponseEntity<List<ParamedicalTask>> getAllTasks(
            @RequestParam(required = false, defaultValue = "All") String status,
            @RequestParam(required = false, defaultValue = "All") String staffType,
            @RequestParam(required = false, defaultValue = "All") String department) {
        return ResponseEntity.ok(taskService.getAllTasks(status, staffType, department));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getTaskSummary() {
        return ResponseEntity.ok(taskService.getTaskSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParamedicalTask> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ParamedicalTask> createTask(@RequestBody ParamedicalTask task) {
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ParamedicalTask> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
