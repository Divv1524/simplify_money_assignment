package com.simplifymoney.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentOrderResponse(
        String paymentOrderId,
        String merchantPurchaseId,
        BigDecimal amount,
        String status,
        Instant createdAt
) {
}
