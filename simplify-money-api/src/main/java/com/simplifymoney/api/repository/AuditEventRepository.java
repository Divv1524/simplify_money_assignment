package com.simplifymoney.api.repository;

import com.simplifymoney.api.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
