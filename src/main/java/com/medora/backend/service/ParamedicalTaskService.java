package com.medora.backend.service;

import com.medora.backend.entity.ParamedicalTask;
import com.medora.backend.repository.ParamedicalTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParamedicalTaskService {

    @Autowired
    private ParamedicalTaskRepository taskRepository;

    public List<ParamedicalTask> getAllTasks(String status, String staffType, String department) {
        List<ParamedicalTask> list = taskRepository.findAll();

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            list = list.stream().filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
        }

        if (staffType != null && !staffType.trim().isEmpty() && !"All".equalsIgnoreCase(staffType)) {
            list = list.stream().filter(t -> t.getStaffType() != null && t.getStaffType().equalsIgnoreCase(staffType)).collect(Collectors.toList());
        }

        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            list = list.stream().filter(t -> t.getDepartment() != null && t.getDepartment().getName().toLowerCase().contains(department.toLowerCase())).collect(Collectors.toList());
        }

        return list;
    }

    public Map<String, Object> getTaskSummary() {
        List<ParamedicalTask> all = taskRepository.findAll();
        long pending = all.stream().filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).count();
        long inProgress = all.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long completed = all.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long cancelled = all.stream().filter(t -> "CANCELLED".equalsIgnoreCase(t.getStatus())).count();

        Map<String, Object> map = new HashMap<>();
        map.put("totalTasks", all.size());
        map.put("pendingTasks", pending);
        map.put("inProgressTasks", inProgress);
        map.put("completedTasks", completed);
        map.put("cancelledTasks", cancelled);
        map.put("currentShift", "Day Operational Shift (08:00 AM - 04:00 PM)");

        return map;
    }

    public Optional<ParamedicalTask> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public ParamedicalTask saveTask(ParamedicalTask task) {
        if (task.getStatus() == null) task.setStatus("PENDING");
        return taskRepository.save(task);
    }

    public ParamedicalTask updateTaskStatus(Long id, String status) {
        ParamedicalTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paramedical task not found with ID: " + id));

        task.setStatus(status.toUpperCase());
        if ("COMPLETED".equalsIgnoreCase(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }
}
