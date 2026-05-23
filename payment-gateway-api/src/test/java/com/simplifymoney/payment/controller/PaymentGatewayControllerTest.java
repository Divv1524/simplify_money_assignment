package com.simplifymoney.payment.controller;

import com.simplifymoney.payment.dto.CreatePaymentOrderRequest;
import com.simplifymoney.payment.dto.ExecutePaymentRequest;
import com.simplifymoney.payment.dto.PaymentExecutionResponse;
import com.simplifymoney.payment.dto.PaymentOrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentGatewayControllerTest {
    @Test
    void executePaymentSucceedsForCreatedOrder() {
        PaymentGatewayController controller = new PaymentGatewayController();
        PaymentOrderResponse order = controller.createOrder(
                "test-request",
                new CreatePaymentOrderRequest("PURCHASE_1", "USER_1", BigDecimal.TEN)
        );

        PaymentExecutionResponse response = controller.executePayment(
                "test-request",
                order.paymentOrderId(),
                new ExecutePaymentRequest("UPI")
        );

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.transactionId()).startsWith("TXN_");
    }
}
