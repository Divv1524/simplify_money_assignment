package com.simplifymoney.api.client;

import com.simplifymoney.api.config.ExternalApiProperties;
import com.simplifymoney.api.dto.GoldRateResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class PartnerGoldClient {
    private final WebClient webClient;

    public PartnerGoldClient(WebClient.Builder builder, ExternalApiProperties properties) {
        this.webClient = builder.baseUrl(properties.partnerGoldBaseUrl()).build();
    }

    public GoldRateResponse currentRate(String requestId) {
        return webClient.get()
                .uri("/partner/gold/rate")
                .header("X-Request-Id", requestId)
                .retrieve()
                .bodyToMono(GoldRateResponse.class)
                .timeout(Duration.ofSeconds(3))
                .block();
    }

    public GoldAllotmentResponse allot(String requestId, GoldAllotmentRequest request) {
        return webClient.post()
                .uri("/partner/gold/allot")
                .header("X-Request-Id", requestId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GoldAllotmentResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block();
    }

    public record GoldAllotmentRequest(String userId, String merchantPurchaseId, BigDecimal amount, BigDecimal ratePerGram) {
    }

    public record GoldAllotmentResponse(
            String partnerOrderId,
            String merchantPurchaseId,
            BigDecimal amount,
            BigDecimal ratePerGram,
            BigDecimal gramsAllotted,
            String status
    ) {
    }
}
