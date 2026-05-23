package com.simplifymoney.api.service;

import com.simplifymoney.api.client.PartnerGoldClient;
import com.simplifymoney.api.client.PaymentGatewayClient;
import com.simplifymoney.api.dto.GoldRateResponse;
import com.simplifymoney.api.dto.InitiatePurchaseRequest;
import com.simplifymoney.api.dto.InitiatePurchaseResponse;
import com.simplifymoney.api.dto.PaymentMethodResponse;
import com.simplifymoney.api.model.GoldPurchase;
import com.simplifymoney.api.model.PaymentRecord;
import com.simplifymoney.api.model.PurchaseStatus;
import com.simplifymoney.api.repository.GoldHoldingRepository;
import com.simplifymoney.api.repository.GoldPurchaseRepository;
import com.simplifymoney.api.repository.PaymentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoldPurchaseServiceTest {
    @Mock
    private PartnerGoldClient partnerGoldClient;
    @Mock
    private PaymentGatewayClient paymentGatewayClient;
    @Mock
    private GoldPurchaseRepository purchaseRepository;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private GoldHoldingRepository holdingRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private GoldPurchaseService service;

    @Test
    void initiateCreatesPurchaseAndPaymentOrder() {
        MDC.put("requestId", "test-request");
        when(purchaseRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(partnerGoldClient.currentRate("test-request")).thenReturn(
                new GoldRateResponse("GOLD", new BigDecimal("7200.50"), new BigDecimal("7100.25"), Instant.now())
        );
        when(purchaseRepository.save(any(GoldPurchase.class))).thenAnswer(invocation -> {
            GoldPurchase purchase = invocation.getArgument(0);
            if (purchase.getId() == null) {
                purchase.setId("PURCHASE_1");
            }
            return purchase;
        });
        when(paymentGatewayClient.createOrder(eq("test-request"), any())).thenReturn(
                new PaymentGatewayClient.PaymentOrderResponse("PAY_1", "PURCHASE_1", BigDecimal.TEN, "CREATED")
        );
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewayClient.methods("test-request")).thenReturn(List.of(
                new PaymentMethodResponse("UPI", "UPI", true)
        ));

        InitiatePurchaseResponse response = service.initiate(
                new InitiatePurchaseRequest("USER_1", BigDecimal.TEN),
                "idem-1"
        );

        assertThat(response.purchaseId()).isEqualTo("PURCHASE_1");
        assertThat(response.paymentOrderId()).isEqualTo("PAY_1");
        assertThat(response.status()).isEqualTo(PurchaseStatus.PAYMENT_PENDING);
        assertThat(response.paymentMethods()).hasSize(1);
        MDC.clear();
    }
}
