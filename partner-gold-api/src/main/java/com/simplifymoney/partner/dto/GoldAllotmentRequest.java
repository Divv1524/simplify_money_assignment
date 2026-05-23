package com.simplifymoney.partner.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GoldAllotmentRequest(
        @NotBlank String userId,
        @NotBlank String merchantPurchaseId,
        @NotNull @DecimalMin("1.00") BigDecimal amount,
        @NotNull @DecimalMin("1.00") BigDecimal ratePerGram
) {
}
