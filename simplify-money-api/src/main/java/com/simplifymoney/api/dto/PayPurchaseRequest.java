package com.simplifymoney.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PayPurchaseRequest(@NotBlank String paymentMethod) {
}
