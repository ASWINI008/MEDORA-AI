package com.medora.backend.controller;

import com.medora.backend.service.HospitalDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for hospital analytics dataset endpoints.
 * Provides both direct Spring Boot DB calculations and proxies to Python AI engine.
 */
@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private static final Logger log = LoggerFactory.getLogger(HospitalController.class);

    @Autowired
    private HospitalDataService hospitalDataService;

    // ── CSV Import ─────────────────────────────────────────────────────────

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(
            @RequestBody(required = false) Map<String, String> body) {

        String csvPath = (body != null && body.containsKey("csvPath"))
                ? body.get("csvPath")
                : "ai-service/data/raw/hospital_data_analysis.csv";

        log.info("CSV import triggered for path: {}", csvPath);
        Map<String, Object> result = hospitalDataService.importFromCSV(csvPath);
        return ResponseEntity.ok(result);
    }

    // ── Core KPIs & Filtered Analytics ──────────────────────────────────────

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKpi() {
        return ResponseEntity.ok(hospitalDataService.getKpiSummary());
    }

    @PostMapping("/filtered-stats")
    public ResponseEntity<Map<String, Object>> getFilteredStats(
            @RequestBody(required = false) Map<String, String> filters) {
        if (filters == null) filters = new HashMap<>();
        return ResponseEntity.ok(hospitalDataService.getFilteredStats(filters));
    }

    // ── Dataset Distributions & Analytics (Proxied to Python ML & Analytics Engine) ─

    @GetMapping("/condition-distribution")
    public ResponseEntity<Object> getConditionDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/condition-distribution"));
    }

    @GetMapping("/procedure-distribution")
    public ResponseEntity<Object> getProcedureDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/procedure-distribution"));
    }

    @GetMapping("/cost-by-condition")
    public ResponseEntity<Object> getCostByCondition() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/cost-by-condition"));
    }

    @GetMapping("/cost-by-procedure")
    public ResponseEntity<Object> getCostByProcedure() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/cost-by-procedure"));
    }

    @GetMapping("/los-by-condition")
    public ResponseEntity<Object> getLosByCondition() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/los-by-condition"));
    }

    @GetMapping("/los-by-procedure")
    public ResponseEntity<Object> getLosByProcedure() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/los-by-procedure"));
    }

    @GetMapping("/readmission-distribution")
    public ResponseEntity<Object> getReadmissionDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/readmission-distribution"));
    }

    @GetMapping("/outcome-distribution")
    public ResponseEntity<Object> getOutcomeDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/outcome-distribution"));
    }

    @GetMapping("/satisfaction-distribution")
    public ResponseEntity<Object> getSatisfactionDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/satisfaction-distribution"));
    }

    @GetMapping("/age-groups")
    public ResponseEntity<Object> getAgeGroups() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/age-groups"));
    }

    @GetMapping("/gender-distribution")
    public ResponseEntity<Object> getGenderDistribution() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/gender-distribution"));
    }

    @GetMapping("/cost-vs-los")
    public ResponseEntity<Object> getCostVsLos() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/cost-vs-los"));
    }

    @GetMapping("/readmission-by-condition")
    public ResponseEntity<Object> getReadmissionByCondition() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/readmission-by-condition"));
    }

    @GetMapping("/readmission-by-procedure")
    public ResponseEntity<Object> getReadmissionByProcedure() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/readmission-by-procedure"));
    }

    @GetMapping("/satisfaction-by-outcome")
    public ResponseEntity<Object> getSatisfactionByOutcome() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/satisfaction-by-outcome"));
    }

    @GetMapping("/los-by-outcome")
    public ResponseEntity<Object> getLosByOutcome() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/los-by-outcome"));
    }

    @GetMapping("/cost-stats")
    public ResponseEntity<Object> getCostStats() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/cost-stats"));
    }

    // ── Additional DB-backed Endpoints ─────────────────────────────────────

    @GetMapping("/condition-stats")
    public ResponseEntity<Map<String, Object>> getConditionStats() {
        List<Map<String, Object>> data = hospitalDataService.getConditionStats();
        Map<String, Object> response = new HashMap<>();
        response.put("conditions", data);
        response.put("count", data.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/procedure-stats")
    public ResponseEntity<Map<String, Object>> getProcedureStats() {
        List<Map<String, Object>> data = hospitalDataService.getProcedureStats();
        Map<String, Object> response = new HashMap<>();
        response.put("procedures", data);
        response.put("count", data.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/readmission-stats")
    public ResponseEntity<Map<String, Object>> getReadmissionStats() {
        return ResponseEntity.ok(hospitalDataService.getReadmissionStats());
    }

    @GetMapping("/outcome-stats")
    public ResponseEntity<Map<String, Object>> getOutcomeStats() {
        return ResponseEntity.ok(hospitalDataService.getOutcomeStats());
    }

    @GetMapping("/gender-stats")
    public ResponseEntity<Map<String, Object>> getGenderStats() {
        return ResponseEntity.ok(hospitalDataService.getGenderStats());
    }

    // ── ML / AI Service Proxies ────────────────────────────────────────────

    @GetMapping("/ml/metrics")
    public ResponseEntity<Object> getMlMetrics() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/ml/metrics"));
    }

    @PostMapping("/ml/predict-los")
    public ResponseEntity<Object> predictLos(@RequestBody Object body) {
        return ResponseEntity.ok(hospitalDataService.proxyPostToAIService("/api/hospital/ml/predict-los", body));
    }

    @PostMapping("/ml/predict-cost")
    public ResponseEntity<Object> predictCost(@RequestBody Object body) {
        return ResponseEntity.ok(hospitalDataService.proxyPostToAIService("/api/hospital/ml/predict-cost", body));
    }

    @PostMapping("/ml/predict-readmission")
    public ResponseEntity<Object> predictReadmission(@RequestBody Object body) {
        return ResponseEntity.ok(hospitalDataService.proxyPostToAIService("/api/hospital/ml/predict-readmission", body));
    }

    // ── Multi-Agent AI Workflow Proxies ────────────────────────────────────

    @GetMapping("/agents/run")
    public ResponseEntity<Object> runAgents() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/agents/run"));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Object> getRecommendations() {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/recommendations"));
    }

    @GetMapping("/analytics/{endpoint}")
    public ResponseEntity<Object> proxyAnalyticsGet(@PathVariable String endpoint) {
        return ResponseEntity.ok(hospitalDataService.proxyGetToAIService("/api/hospital/analytics/" + endpoint));
    }
}
