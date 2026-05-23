package com.simplifymoney.api.dto;

import com.simplifymoney.api.model.PurchaseStatus;

import java.math.BigDecimal;
import java.util.List;

public record InitiatePurchaseResponse(
        String purchaseId,
        String paymentOrderId,
        BigDecimal amount,
        BigDecimal ratePerGram,
        PurchaseStatus status,
        List<PaymentMethodResponse> paymentMethods
) {
}
