package com.medora.backend.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MockAbdmIntegrationServiceImpl implements AbdmIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(MockAbdmIntegrationServiceImpl.class);

    @Override
    public String generateAadhaarOtp(String aadhaarNumber) {
        logger.info("[ABDM INTEGRATION - PLANNED/READY] Generating mock OTP for Aadhaar: {}", aadhaarNumber);
        return UUID.randomUUID().toString(); // Mock txnId
    }

    @Override
    public boolean verifyAadhaarOtp(String txnId, String otp) {
        logger.info("[ABDM INTEGRATION - PLANNED/READY] Verifying mock OTP: {} for Txn: {}", otp, txnId);
        return true; // Mock success
    }

    @Override
    public boolean verifyAbhaNumber(String abhaNumber) {
        logger.info("[ABDM INTEGRATION - PLANNED/READY] Verifying mock ABHA number: {}", abhaNumber);
        return true; // Mock success
    }

    @Override
    public String requestConsent(Long patientId, Long doctorId, String purpose) {
        logger.info("[ABDM INTEGRATION - PLANNED/READY] Requesting consent for Patient ID: {}, Doctor ID: {}, Purpose: {}", patientId, doctorId, purpose);
        return UUID.randomUUID().toString(); // Mock Consent Artefact ID
    }
}
