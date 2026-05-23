package com.simplifymoney.partner.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GoldAllotmentResponse(
        String partnerOrderId,
        String merchantPurchaseId,
        BigDecimal amount,
        BigDecimal ratePerGram,
        BigDecimal gramsAllotted,
        String status,
        Instant allottedAt
) {
}
