package com.medora.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.medora.backend.repository.HospitalRecordRepository;

import java.io.File;
import java.util.Map;

/**
 * Automatically imports the hospital dataset CSV on application startup
 * if the hospital_records table is empty.
 *
 * It tries several candidate paths relative to the working directory so the
 * import works whether Maven runs from the project root or from medora-backend/.
 */
@Component
public class HospitalDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(HospitalDataInitializer.class);

    /** Candidate paths tried in order (relative to CWD). */
    private static final String[] CSV_CANDIDATES = {
        "ai-service/data/raw/hospital_data_analysis.csv",
        "../ai-service/data/raw/hospital_data_analysis.csv",
        "data/hospital_data_analysis.csv",
        "hospital_data_analysis.csv"
    };

    @Autowired
    private HospitalRecordRepository repository;

    @Autowired
    private HospitalDataService hospitalDataService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        long existingCount = repository.count();
        if (existingCount > 0) {
            log.info("Hospital records table already has {} rows — skipping auto-import.", existingCount);
            return;
        }

        log.info("Hospital records table is empty — attempting auto-import from CSV...");
        String resolvedPath = resolveCsvPath();

        if (resolvedPath == null) {
            log.warn("Hospital dataset CSV not found in any candidate location. " +
                     "Use POST /api/hospital/import with {{\"csvPath\": \"<path>\"}} to import manually.");
            return;
        }

        log.info("Auto-importing hospital dataset from: {}", resolvedPath);
        try {
            Map<String, Object> result = hospitalDataService.importFromCSV(resolvedPath);
            log.info("Auto-import result — imported: {}, skipped: {}, errors: {}",
                    result.get("imported"), result.get("skipped"), result.get("errors"));
        } catch (Exception e) {
            log.error("Auto-import failed: {}", e.getMessage(), e);
        }
    }

    private String resolveCsvPath() {
        for (String candidate : CSV_CANDIDATES) {
            File f = new File(candidate);
            if (f.exists() && f.isFile()) {
                log.debug("Found CSV at: {}", f.getAbsolutePath());
                return f.getAbsolutePath();
            }
        }
        return null;
    }
}
