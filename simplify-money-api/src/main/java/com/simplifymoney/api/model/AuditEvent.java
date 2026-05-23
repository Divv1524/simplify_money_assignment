package com.simplifymoney.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("audit_events")
public class AuditEvent {
    @Id
    private String id;
    private String requestId;
    private String eventType;
    private String entityId;
    private Map<String, Object> metadata;
    private Instant createdAt;

    public AuditEvent() {
    }

    public AuditEvent(String requestId, String eventType, String entityId, Map<String, Object> metadata) {
        this.requestId = requestId;
        this.eventType = eventType;
        this.entityId = entityId;
        this.metadata = metadata;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRequestId() { return requestId; }
    public String getEventType() { return eventType; }
    public String getEntityId() { return entityId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
