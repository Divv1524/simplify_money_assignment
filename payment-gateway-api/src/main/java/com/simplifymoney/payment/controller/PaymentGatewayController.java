package com.simplifymoney.payment.controller;

import com.simplifymoney.payment.dto.CreatePaymentOrderRequest;
import com.simplifymoney.payment.dto.ExecutePaymentRequest;
import com.simplifymoney.payment.dto.PaymentExecutionResponse;
import com.simplifymoney.payment.dto.PaymentMethodResponse;
import com.simplifymoney.payment.dto.PaymentOrderResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/payment")
public class PaymentGatewayController {
    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayController.class);
    private final Map<String, PaymentOrderResponse> orders = new ConcurrentHashMap<>();

    @GetMapping("/methods")
    public List<PaymentMethodResponse> methods(@RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        log.info("Returning payment methods requestId={}", requestId);
        return List.of(
                new PaymentMethodResponse("UPI", "UPI", true),
                new PaymentMethodResponse("CARD", "Debit/Credit Card", true),
                new PaymentMethodResponse("NET_BANKING", "Net Banking", true)
        );
    }

    @PostMapping("/orders")
    public PaymentOrderResponse createOrder(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody CreatePaymentOrderRequest request
    ) {
        String paymentOrderId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        PaymentOrderResponse response = new PaymentOrderResponse(
                paymentOrderId,
                request.merchantPurchaseId(),
                request.amount(),
                "CREATED",
                Instant.now()
        );
        orders.put(paymentOrderId, response);
        log.info("Payment order created requestId={} paymentOrderId={} merchantPurchaseId={} userId={}",
                requestId, paymentOrderId, request.merchantPurchaseId(), request.userId());
        return response;
    }

    @PostMapping("/orders/{paymentOrderId}/pay")
    public PaymentExecutionResponse executePayment(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String paymentOrderId,
            @Valid @RequestBody ExecutePaymentRequest request
    ) {
        if (!orders.containsKey(paymentOrderId)) {
            return new PaymentExecutionResponse(paymentOrderId, "FAILED", null, "Payment order not found", Instant.now());
        }
        if ("FAIL".equalsIgnoreCase(request.paymentMethod())) {
            log.info("Mock payment failed requestId={} paymentOrderId={}", requestId, paymentOrderId);
            return new PaymentExecutionResponse(paymentOrderId, "FAILED", null, "Mock payment failure", Instant.now());
        }
        String transactionId = "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.info("Mock payment successful requestId={} paymentOrderId={} paymentMethod={} transactionId={}",
                requestId, paymentOrderId, request.paymentMethod(), transactionId);
        return new PaymentExecutionResponse(paymentOrderId, "SUCCESS", transactionId, "Payment successful", Instant.now());
    }
}
