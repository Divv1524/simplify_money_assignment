package com.simplifymoney.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("gold_holdings")
public class GoldHolding {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private BigDecimal totalGrams = BigDecimal.ZERO;
    private BigDecimal investedAmount = BigDecimal.ZERO;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getTotalGrams() { return totalGrams; }
    public void setTotalGrams(BigDecimal totalGrams) { this.totalGrams = totalGrams; }
    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) { this.investedAmount = investedAmount; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
