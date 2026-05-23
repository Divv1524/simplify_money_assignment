package com.simplifymoney.api.dto;

import java.math.BigDecimal;

public record PortfolioResponse(
        String userId,
        BigDecimal totalGrams,
        BigDecimal investedAmount,
        BigDecimal currentRatePerGram,
        BigDecimal currentValue,
        BigDecimal gainLossAmount,
        BigDecimal gainLossPercentage
) {
}
