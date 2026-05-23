package com.simplifymoney.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("gold_purchases")
public class GoldPurchase {
    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed(unique = true, sparse = true)
    private String idempotencyKey;
    private BigDecimal amount;
    private BigDecimal ratePerGram;
    private BigDecimal gramsAllotted;
    private PurchaseStatus status;
    private String paymentOrderId;
    private String partnerOrderId;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRatePerGram() { return ratePerGram; }
    public void setRatePerGram(BigDecimal ratePerGram) { this.ratePerGram = ratePerGram; }
    public BigDecimal getGramsAllotted() { return gramsAllotted; }
    public void setGramsAllotted(BigDecimal gramsAllotted) { this.gramsAllotted = gramsAllotted; }
    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) { this.status = status; }
    public String getPaymentOrderId() { return paymentOrderId; }
    public void setPaymentOrderId(String paymentOrderId) { this.paymentOrderId = paymentOrderId; }
    public String getPartnerOrderId() { return partnerOrderId; }
    public void setPartnerOrderId(String partnerOrderId) { this.partnerOrderId = partnerOrderId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
