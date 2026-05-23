package com.simplifymoney.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GoldRateResponse(String metal, BigDecimal buyPricePerGram, BigDecimal sellPricePerGram, Instant validUntil) {
}
