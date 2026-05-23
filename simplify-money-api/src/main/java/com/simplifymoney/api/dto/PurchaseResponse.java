package com.simplifymoney.api.dto;

import com.simplifymoney.api.model.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseResponse(
        String purchaseId,
        String userId,
        BigDecimal amount,
        BigDecimal ratePerGram,
        BigDecimal gramsAllotted,
        PurchaseStatus status,
        String paymentOrderId,
        String partnerOrderId,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
}
