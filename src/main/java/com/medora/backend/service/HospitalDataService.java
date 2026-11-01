package com.medora.backend.service;

import com.medora.backend.entity.HospitalRecord;
import com.medora.backend.repository.HospitalRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core service for the hospital analytics dataset.
 * Handles CSV import, all aggregation/stats, and proxying to the Python AI service.
 */
@Service
public class HospitalDataService {

    private static final Logger log = LoggerFactory.getLogger(HospitalDataService.class);

    @Autowired
    private HospitalRecordRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    // ── CSV Import ─────────────────────────────────────────────────────────

    /**
     * Reads a CSV file and imports rows, skipping duplicates by patientExternalId.
     *
     * @param csvFilePath absolute or relative path to the CSV file
     * @return map with keys: imported, skipped, total
     */
    public Map<String, Object> importFromCSV(String csvFilePath) {
        int imported = 0;
        int skipped = 0;
        int errors = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine(); // consume header
            if (headerLine == null) {
                log.warn("CSV file is empty: {}", csvFilePath);
                return buildImportResult(0, 0, 0, "File is empty");
            }

            log.info("CSV header: {}", headerLine);
            String line;
            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    HospitalRecord record = parseCsvLine(line);
                    if (record == null) {
                        errors++;
                        continue;
                    }

                    if (repository.existsByPatientExternalId(record.getPatientExternalId())) {
                        skipped++;
                    } else {
                        repository.save(record);
                        imported++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse line {}: {} — {}", lineNum, line, e.getMessage());
                    errors++;
                }
            }
        } catch (IOException e) {
            log.error("Cannot read CSV file '{}': {}", csvFilePath, e.getMessage());
            return buildImportResult(0, 0, 0, "File not found or unreadable: " + e.getMessage());
        }

        log.info("CSV import complete — imported: {}, skipped: {}, errors: {}", imported, skipped, errors);
        return buildImportResult(imported, skipped, errors, "Import complete");
    }

    /**
     * Parses one CSV line.
     * Expected column order: Patient_ID, Age, Gender, Condition, Procedure, Cost,
     *                        Length_of_Stay, Readmission, Outcome, Satisfaction
     */
    private HospitalRecord parseCsvLine(String line) {
        // Handle quoted fields with commas inside by a simple state-machine split
        List<String> cols = splitCsvLine(line);
        if (cols.size() < 10) {
            log.warn("Insufficient columns ({}) in line: {}", cols.size(), line);
            return null;
        }

        try {
            Integer patientId  = Integer.parseInt(cols.get(0).trim());
            Integer age        = Integer.parseInt(cols.get(1).trim());
            String  gender     = cols.get(2).trim();
            String  condition  = cols.get(3).trim();
            String  procedure  = cols.get(4).trim();
            Double  cost       = Double.parseDouble(cols.get(5).trim());
            Integer los        = Integer.parseInt(cols.get(6).trim());
            String  readmission= cols.get(7).trim();
            String  outcome    = cols.get(8).trim();
            Integer satisfaction = Integer.parseInt(cols.get(9).trim());

            return new HospitalRecord(patientId, age, gender, condition, procedure,
                    cost, los, readmission, outcome, satisfaction, LocalDateTime.now());
        } catch (NumberFormatException e) {
            log.warn("Number parse error in line '{}': {}", line, e.getMessage());
            return null;
        }
    }

    /** Simple CSV line splitter that handles double-quoted fields. */
    private List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    private Map<String, Object> buildImportResult(int imported, int skipped, int errors, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        result.put("total", imported + skipped + errors);
        result.put("message", message);
        return result;
    }

    // ── KPI Summary ───────────────────────────────────────────────────────

    public Map<String, Object> getKpiSummary() {
        long total = repository.count();
        Double avgAge  = repository.findAvgAge();
        Double avgCost = repository.findAvgCost();
        Double avgLos  = repository.findAvgLengthOfStay();
        Double avgSat  = repository.findAvgSatisfaction();

        long readYes = repository.countByReadmission("Yes");
        double readmissionRate = total > 0 ? Math.round((readYes * 100.0 / total) * 100.0) / 100.0 : 0.0;

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalRecords",      total);
        kpi.put("avgAge",            round2(avgAge));
        kpi.put("avgCost",           round2(avgCost));
        kpi.put("avgLos",            round2(avgLos));
        kpi.put("readmissionRate",   readmissionRate);
        kpi.put("avgSatisfaction",   round2(avgSat));
        return kpi;
    }

    // ── Condition Stats ────────────────────────────────────────────────────

    public List<Map<String, Object>> getConditionStats() {
        List<Object[]> rows = repository.findConditionStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("condition", row[0]);
            m.put("count",     row[1]);
            m.put("avgCost",   round2((Double) row[2]));
            m.put("avgLos",    round2((Double) row[3]));
            result.add(m);
        }
        return result;
    }

    // ── Procedure Stats ────────────────────────────────────────────────────

    public List<Map<String, Object>> getProcedureStats() {
        List<Object[]> rows = repository.findProcedureStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("procedure", row[0]);
            m.put("count",     row[1]);
            m.put("avgCost",   round2((Double) row[2]));
            m.put("avgLos",    round2((Double) row[3]));
            result.add(m);
        }
        return result;
    }

    // ── Readmission Stats ──────────────────────────────────────────────────

    public Map<String, Object> getReadmissionStats() {
        long total  = repository.count();
        long yes    = repository.countByReadmission("Yes");
        long no     = repository.countByReadmission("No");
        double rate = total > 0 ? Math.round((yes * 100.0 / total) * 100.0) / 100.0 : 0.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalYes",    yes);
        stats.put("totalNo",     no);
        stats.put("ratePercent", rate);
        return stats;
    }

    // ── Outcome Stats ──────────────────────────────────────────────────────

    public Map<String, Object> getOutcomeStats() {
        List<Object[]> rows = repository.findOutcomeGroupCounts();
        Map<String, Object> stats = new LinkedHashMap<>();
        long total = 0;
        long recovered = 0;
        for (Object[] row : rows) {
            String outcome = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            stats.put(outcome, count);
            total += count;
            if ("Recovered".equalsIgnoreCase(outcome)) recovered = count;
        }
        double recoverRate = total > 0 ? Math.round((recovered * 100.0 / total) * 100.0) / 100.0 : 0.0;
        stats.put("recoverRate", recoverRate);
        return stats;
    }

    // ── Gender Stats ───────────────────────────────────────────────────────

    public Map<String, Object> getGenderStats() {
        List<Object[]> rows = repository.findGenderGroupCounts();
        Map<String, Object> stats = new LinkedHashMap<>();
        for (Object[] row : rows) {
            stats.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return stats;
    }

    // ── Age Group Stats ────────────────────────────────────────────────────

    public Map<String, Object> getAgeGroupStats() {
        List<Integer> ages = repository.findAllAges();

        Map<String, Long> groups = new LinkedHashMap<>();
        groups.put("Under 25", 0L);
        groups.put("25-34",    0L);
        groups.put("35-44",    0L);
        groups.put("45-54",    0L);
        groups.put("55-64",    0L);
        groups.put("65+",      0L);

        for (Integer age : ages) {
            if (age < 25)       groups.merge("Under 25", 1L, Long::sum);
            else if (age <= 34) groups.merge("25-34",    1L, Long::sum);
            else if (age <= 44) groups.merge("35-44",    1L, Long::sum);
            else if (age <= 54) groups.merge("45-54",    1L, Long::sum);
            else if (age <= 64) groups.merge("55-64",    1L, Long::sum);
            else                groups.merge("65+",      1L, Long::sum);
        }

        return new LinkedHashMap<>(groups);
    }

    // ── Cost Stats ─────────────────────────────────────────────────────────

    public Map<String, Object> getCostStats() {
        Object[] minMaxAvg = repository.findCostMinMaxAvg();
        List<Double> sorted = repository.findAllCostsSorted();

        double median = 0.0;
        if (!sorted.isEmpty()) {
            int mid = sorted.size() / 2;
            median = sorted.size() % 2 == 0
                    ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0
                    : sorted.get(mid);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("min",    round2((Double) minMaxAvg[0]));
        stats.put("max",    round2((Double) minMaxAvg[1]));
        stats.put("avg",    round2((Double) minMaxAvg[2]));
        stats.put("median", round2(median));
        return stats;
    }

    // ── Satisfaction Distribution ──────────────────────────────────────────

    public Map<String, Object> getSatisfactionDistribution() {
        List<Object[]> rows = repository.findSatisfactionDistribution();
        Map<String, Object> dist = new LinkedHashMap<>();
        for (Object[] row : rows) {
            dist.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return dist;
    }

    // ── Filtered Stats ─────────────────────────────────────────────────────

    /**
     * Returns filtered analytics based on optional gender, condition, readmission filters.
     *
     * @param filters map of filter keys: gender, condition, readmission
     * @return summary stats for the filtered subset
     */
    public Map<String, Object> getFilteredStats(Map<String, String> filters) {
        String gender     = filters.getOrDefault("gender", null);
        String condition  = filters.getOrDefault("condition", null);
        String readmission= filters.getOrDefault("readmission", null);

        // Start with all records, then narrow in-memory (dataset is small: 984 records)
        List<HospitalRecord> records = repository.findAll();

        if (gender != null && !gender.isBlank()) {
            records = records.stream().filter(r -> gender.equalsIgnoreCase(r.getGender())).toList();
        }
        if (condition != null && !condition.isBlank()) {
            records = records.stream().filter(r -> condition.equalsIgnoreCase(r.getCondition())).toList();
        }
        if (readmission != null && !readmission.isBlank()) {
            records = records.stream().filter(r -> readmission.equalsIgnoreCase(r.getReadmission())).toList();
        }

        long total = records.size();
        double avgAge  = records.stream().mapToInt(HospitalRecord::getAge).average().orElse(0);
        double avgCost = records.stream().mapToDouble(HospitalRecord::getCost).average().orElse(0);
        double avgLos  = records.stream().mapToInt(HospitalRecord::getLengthOfStay).average().orElse(0);
        double avgSat  = records.stream().mapToInt(HospitalRecord::getSatisfaction).average().orElse(0);
        long readYes   = records.stream().filter(r -> "Yes".equalsIgnoreCase(r.getReadmission())).count();
        double readRate= total > 0 ? Math.round((readYes * 100.0 / total) * 100.0) / 100.0 : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("appliedFilters", filters);
        result.put("totalRecords",   total);
        result.put("avgAge",         round2(avgAge));
        result.put("avgCost",        round2(avgCost));
        result.put("avgLos",         round2(avgLos));
        result.put("avgSatisfaction",round2(avgSat));
        result.put("readmissionRate",readRate);
        return result;
    }

    // ── AI Service Proxy ───────────────────────────────────────────────────

    /**
     * Proxies a GET request to the Python AI service.
     *
     * @param endpoint relative endpoint path (e.g. "/api/hospital/predict-readmission")
     * @return raw response body (can be List or Map)
     */
    public Object proxyGetToAIService(String endpoint) {
        String url = aiServiceUrl + endpoint;
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (RestClientException e) {
            log.error("AI service GET proxy error for {}: {}", url, e.getMessage());
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "AI service unavailable");
            error.put("detail", e.getMessage());
            error.put("endpoint", endpoint);
            return error;
        }
    }

    /**
     * Proxies a POST request to the Python AI service, forwarding the request body.
     *
     * @param endpoint    relative endpoint path
     * @param requestBody map or object of request payload
     * @return raw response body
     */
    public Object proxyPostToAIService(String endpoint, Object requestBody) {
        String url = aiServiceUrl + endpoint;
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, requestBody, Object.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (RestClientException e) {
            log.error("AI service POST proxy error for {}: {}", url, e.getMessage());
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "AI service unavailable");
            error.put("detail", e.getMessage());
            error.put("endpoint", endpoint);
            return error;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private double round2(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }
}
