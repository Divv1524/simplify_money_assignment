package com.simplifymoney.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InitiatePurchaseRequest(
        @NotBlank String userId,
        @NotNull @DecimalMin("1.00") BigDecimal amount
) {
}
