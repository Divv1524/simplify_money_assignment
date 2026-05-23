package com.simplifymoney.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GoldHoldingResponse(
        String userId,
        BigDecimal totalGrams,
        BigDecimal investedAmount,
        Instant updatedAt
) {
}
