package com.medora.backend.service;

import com.medora.backend.entity.AuditLog;
import com.medora.backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String action, String username, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog(action, username, details, ipAddress);
        auditLogRepository.save(auditLog);
    }
}
