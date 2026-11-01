package com.medora.backend.integration;

public interface AbdmIntegrationService {
    
    /**
     * Generate OTP for Aadhaar/ABHA linking
     */
    String generateAadhaarOtp(String aadhaarNumber);

    /**
     * Verify OTP
     */
    boolean verifyAadhaarOtp(String txnId, String otp);

    /**
     * Verify if the given ABHA number is active and registered
     */
    boolean verifyAbhaNumber(String abhaNumber);

    /**
     * Request consent from patient to view health records
     */
    String requestConsent(Long patientId, Long doctorId, String purpose);
}
