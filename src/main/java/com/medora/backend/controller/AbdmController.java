package com.medora.backend.controller;

import com.medora.backend.integration.AbdmIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/abdm")
public class AbdmController {

    @Autowired
    private AbdmIntegrationService abdmIntegrationService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getIntegrationStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ABDM/ABHA Integration - Planned/Integration Ready");
        status.put("message", "The API integration layer is structurally ready for sandbox credentials.");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<Map<String, String>> generateOtp(@RequestBody Map<String, String> request) {
        String txnId = abdmIntegrationService.generateAadhaarOtp(request.get("aadhaarNumber"));
        Map<String, String> response = new HashMap<>();
        response.put("txnId", txnId);
        response.put("message", "Mock OTP generated (ABDM/ABHA Integration - Planned/Integration Ready)");
        return ResponseEntity.ok(response);
    }
}
