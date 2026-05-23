package com.simplifymoney.payment.dto;

import java.time.Instant;

public record PaymentExecutionResponse(
        String paymentOrderId,
        String status,
        String transactionId,
        String message,
        Instant paidAt
) {
}
