package com.simplifymoney.api.service;

import com.simplifymoney.api.model.AuditEvent;
import com.simplifymoney.api.repository.AuditEventRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(String eventType, String entityId, Map<String, Object> metadata) {
        auditEventRepository.save(new AuditEvent(MDC.get("requestId"), eventType, entityId, metadata));
    }
}
