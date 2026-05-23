package com.simplifymoney.api.client;

import com.simplifymoney.api.config.ExternalApiProperties;
import com.simplifymoney.api.dto.PaymentMethodResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Component
public class PaymentGatewayClient {
    private final WebClient webClient;

    public PaymentGatewayClient(WebClient.Builder builder, ExternalApiProperties properties) {
        this.webClient = builder.baseUrl(properties.paymentGatewayBaseUrl()).build();
    }

    public List<PaymentMethodResponse> methods(String requestId) {
        return webClient.get()
                .uri("/payment/methods")
                .header("X-Request-Id", requestId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PaymentMethodResponse>>() {})
                .timeout(Duration.ofSeconds(3))
                .block();
    }

    public PaymentOrderResponse createOrder(String requestId, CreatePaymentOrderRequest request) {
        return webClient.post()
                .uri("/payment/orders")
                .header("X-Request-Id", requestId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentOrderResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block();
    }

    public PaymentExecutionResponse execute(String requestId, String paymentOrderId, ExecutePaymentRequest request) {
        return webClient.post()
                .uri("/payment/orders/{paymentOrderId}/pay", paymentOrderId)
                .header("X-Request-Id", requestId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentExecutionResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block();
    }

    public record CreatePaymentOrderRequest(String merchantPurchaseId, String userId, BigDecimal amount) {
    }

    public record PaymentOrderResponse(String paymentOrderId, String merchantPurchaseId, BigDecimal amount, String status) {
    }

    public record ExecutePaymentRequest(String paymentMethod) {
    }

    public record PaymentExecutionResponse(String paymentOrderId, String status, String transactionId, String message) {
    }
}
