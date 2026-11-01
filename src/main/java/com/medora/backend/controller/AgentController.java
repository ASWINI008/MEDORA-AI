package com.medora.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/status")
    public ResponseEntity<?> getAgentsStatus() {
        try {
            String targetUrl = aiServiceUrl + "/api/ai/health";
            Map health = restTemplate.getForObject(targetUrl, Map.class);
            
            return ResponseEntity.ok(Map.of(
                "ai_agent_engine_status", "ACTIVE",
                "ml_loaded", health.getOrDefault("ml_loaded", true),
                "agents", List.of(
                    Map.of("name", "Patient Flow Agent", "status", "ACTIVE"),
                    Map.of("name", "Staff Agent", "status", "ACTIVE"),
                    Map.of("name", "Resource Agent", "status", "ACTIVE"),
                    Map.of("name", "Appointment Agent", "status", "ACTIVE"),
                    Map.of("name", "Ambulance Agent", "status", "ACTIVE"),
                    Map.of("name", "Prediction Agent", "status", "ACTIVE"),
                    Map.of("name", "Recommendation Agent", "status", "ACTIVE")
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "ai_agent_engine_status", "INACTIVE",
                "ml_loaded", false,
                "message", "Python AI Engine Service is offline."
            ));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> triggerAgentAnalysis(@RequestBody Map<String, Object> requestPayload) {
        try {
            String targetUrl = aiServiceUrl + "/api/ai/explain";
            ResponseEntity<Map> response = restTemplate.postForEntity(targetUrl, requestPayload, Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "ERROR",
                "message", "Failed to communicate with AI agent engine at " + aiServiceUrl
            ));
        }
    }
}

