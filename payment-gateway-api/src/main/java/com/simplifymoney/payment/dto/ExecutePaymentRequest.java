package com.simplifymoney.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record ExecutePaymentRequest(@NotBlank String paymentMethod) {
}
