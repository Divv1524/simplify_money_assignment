package com.simplifymoney.api.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String requestId,
        String message,
        Map<String, String> errors,
        Instant timestamp
) {
}
