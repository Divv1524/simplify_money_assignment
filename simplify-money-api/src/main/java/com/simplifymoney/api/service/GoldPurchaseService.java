package com.simplifymoney.api.service;

import com.simplifymoney.api.client.PartnerGoldClient;
import com.simplifymoney.api.client.PaymentGatewayClient;
import com.simplifymoney.api.dto.GoldRateResponse;
import com.simplifymoney.api.dto.GoldHoldingResponse;
import com.simplifymoney.api.dto.InitiatePurchaseRequest;
import com.simplifymoney.api.dto.InitiatePurchaseResponse;
import com.simplifymoney.api.dto.PayPurchaseRequest;
import com.simplifymoney.api.dto.PaymentMethodResponse;
import com.simplifymoney.api.dto.PortfolioResponse;
import com.simplifymoney.api.dto.PurchaseResponse;
import com.simplifymoney.api.exception.InvalidStateException;
import com.simplifymoney.api.exception.NotFoundException;
import com.simplifymoney.api.model.GoldHolding;
import com.simplifymoney.api.model.GoldPurchase;
import com.simplifymoney.api.model.PaymentRecord;
import com.simplifymoney.api.model.PaymentStatus;
import com.simplifymoney.api.model.PurchaseStatus;
import com.simplifymoney.api.repository.GoldHoldingRepository;
import com.simplifymoney.api.repository.GoldPurchaseRepository;
import com.simplifymoney.api.repository.PaymentRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class GoldPurchaseService {
    private static final Logger log = LoggerFactory.getLogger(GoldPurchaseService.class);

    private final PartnerGoldClient partnerGoldClient;
    private final PaymentGatewayClient paymentGatewayClient;
    private final GoldPurchaseRepository purchaseRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final GoldHoldingRepository holdingRepository;
    private final AuditService auditService;

    public GoldPurchaseService(
            PartnerGoldClient partnerGoldClient,
            PaymentGatewayClient paymentGatewayClient,
            GoldPurchaseRepository purchaseRepository,
            PaymentRecordRepository paymentRecordRepository,
            GoldHoldingRepository holdingRepository,
            AuditService auditService
    ) {
        this.partnerGoldClient = partnerGoldClient;
        this.paymentGatewayClient = paymentGatewayClient;
        this.purchaseRepository = purchaseRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.holdingRepository = holdingRepository;
        this.auditService = auditService;
    }

    public GoldRateResponse currentRate() {
        return partnerGoldClient.currentRate(requestId());
    }

    public List<PaymentMethodResponse> paymentMethods() {
        return paymentGatewayClient.methods(requestId());
    }

    public InitiatePurchaseResponse initiate(InitiatePurchaseRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = purchaseRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                GoldPurchase purchase = existing.get();
                log.info("Returning idempotent purchase userId={} purchaseId={}", purchase.getUserId(), purchase.getId());
                return new InitiatePurchaseResponse(
                        purchase.getId(),
                        purchase.getPaymentOrderId(),
                        purchase.getAmount(),
                        purchase.getRatePerGram(),
                        purchase.getStatus(),
                        paymentMethods()
                );
            }
        }

        GoldRateResponse rate = currentRate();
        Instant now = Instant.now();
        GoldPurchase purchase = new GoldPurchase();
        purchase.setUserId(request.userId());
        purchase.setAmount(request.amount());
        purchase.setRatePerGram(rate.buyPricePerGram());
        purchase.setStatus(PurchaseStatus.INITIATED);
        purchase.setIdempotencyKey(idempotencyKey);
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);
        purchase = purchaseRepository.save(purchase);

        PaymentGatewayClient.PaymentOrderResponse paymentOrder = paymentGatewayClient.createOrder(
                requestId(),
                new PaymentGatewayClient.CreatePaymentOrderRequest(purchase.getId(), request.userId(), request.amount())
        );

        purchase.setPaymentOrderId(paymentOrder.paymentOrderId());
        purchase.setStatus(PurchaseStatus.PAYMENT_PENDING);
        purchase.setUpdatedAt(Instant.now());
        purchaseRepository.save(purchase);

        PaymentRecord payment = new PaymentRecord();
        payment.setPurchaseId(purchase.getId());
        payment.setPaymentOrderId(paymentOrder.paymentOrderId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        paymentRecordRepository.save(payment);

        List<PaymentMethodResponse> methods = paymentMethods();
        auditService.record("PURCHASE_INITIATED", purchase.getId(), Map.of("userId", request.userId(), "amount", request.amount()));
        log.info("Purchase initiated userId={} purchaseId={} paymentOrderId={} amount={} rate={}",
                request.userId(), purchase.getId(), paymentOrder.paymentOrderId(), request.amount(), rate.buyPricePerGram());

        return new InitiatePurchaseResponse(
                purchase.getId(),
                paymentOrder.paymentOrderId(),
                purchase.getAmount(),
                purchase.getRatePerGram(),
                purchase.getStatus(),
                methods
        );
    }

    public PurchaseResponse pay(String purchaseId, PayPurchaseRequest request) {
        GoldPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found: " + purchaseId));
        if (purchase.getStatus() != PurchaseStatus.PAYMENT_PENDING) {
            throw new InvalidStateException("Purchase cannot be paid in status " + purchase.getStatus());
        }

        PaymentRecord payment = paymentRecordRepository.findByPurchaseId(purchaseId)
                .orElseThrow(() -> new NotFoundException("Payment not found for purchase: " + purchaseId));

        PaymentGatewayClient.PaymentExecutionResponse execution = paymentGatewayClient.execute(
                requestId(),
                purchase.getPaymentOrderId(),
                new PaymentGatewayClient.ExecutePaymentRequest(request.paymentMethod())
        );

        payment.setPaymentMethod(request.paymentMethod());
        payment.setUpdatedAt(Instant.now());

        if (!"SUCCESS".equalsIgnoreCase(execution.status())) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRecordRepository.save(payment);
            purchase.setStatus(PurchaseStatus.FAILED);
            purchase.setFailureReason(execution.message());
            purchase.setUpdatedAt(Instant.now());
            purchaseRepository.save(purchase);
            auditService.record("PAYMENT_FAILED", purchaseId, Map.of("reason", execution.message()));
            log.info("Payment failed userId={} purchaseId={} reason={}", purchase.getUserId(), purchaseId, execution.message());
            return toPurchaseResponse(purchase);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(execution.transactionId());
        paymentRecordRepository.save(payment);
        purchase.setStatus(PurchaseStatus.PAYMENT_SUCCESS);
        purchase.setUpdatedAt(Instant.now());
        purchaseRepository.save(purchase);

        PartnerGoldClient.GoldAllotmentResponse allotment = partnerGoldClient.allot(
                requestId(),
                new PartnerGoldClient.GoldAllotmentRequest(
                        purchase.getUserId(),
                        purchase.getId(),
                        purchase.getAmount(),
                        purchase.getRatePerGram()
                )
        );

        purchase.setPartnerOrderId(allotment.partnerOrderId());
        purchase.setGramsAllotted(allotment.gramsAllotted());
        purchase.setStatus(PurchaseStatus.GOLD_ALLOCATED);
        purchase.setUpdatedAt(Instant.now());
        purchaseRepository.save(purchase);
        updateHolding(purchase);

        auditService.record("GOLD_ALLOCATED", purchaseId, Map.of(
                "userId", purchase.getUserId(),
                "grams", allotment.gramsAllotted(),
                "partnerOrderId", allotment.partnerOrderId()
        ));
        log.info("Gold purchase completed userId={} purchaseId={} grams={} partnerOrderId={}",
                purchase.getUserId(), purchaseId, allotment.gramsAllotted(), allotment.partnerOrderId());

        return toPurchaseResponse(purchase);
    }

    public PurchaseResponse getPurchase(String purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .map(this::toPurchaseResponse)
                .orElseThrow(() -> new NotFoundException("Purchase not found: " + purchaseId));
    }

    public List<PurchaseResponse> purchasesByUser(String userId) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toPurchaseResponse)
                .toList();
    }

    public GoldHoldingResponse holding(String userId) {
        GoldHolding holding = holdingRepository.findByUserId(userId).orElseGet(() -> {
            GoldHolding empty = new GoldHolding();
            empty.setUserId(userId);
            empty.setTotalGrams(BigDecimal.ZERO);
            empty.setInvestedAmount(BigDecimal.ZERO);
            return empty;
        });
        return new GoldHoldingResponse(
                userId,
                holding.getTotalGrams(),
                holding.getInvestedAmount(),
                holding.getUpdatedAt()
        );
    }

    public PurchaseResponse cancel(String purchaseId) {
        GoldPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found: " + purchaseId));
        if (purchase.getStatus() != PurchaseStatus.PAYMENT_PENDING) {
            throw new InvalidStateException("Only PAYMENT_PENDING purchases can be cancelled. Current status: " + purchase.getStatus());
        }
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setFailureReason("Cancelled by user before payment completion");
        purchase.setUpdatedAt(Instant.now());
        purchaseRepository.save(purchase);
        auditService.record("PURCHASE_CANCELLED", purchaseId, Map.of("userId", purchase.getUserId()));
        log.info("Purchase cancelled userId={} purchaseId={}", purchase.getUserId(), purchaseId);
        return toPurchaseResponse(purchase);
    }

    public PortfolioResponse portfolio(String userId) {
        GoldHolding holding = holdingRepository.findByUserId(userId).orElseGet(() -> {
            GoldHolding empty = new GoldHolding();
            empty.setUserId(userId);
            empty.setTotalGrams(BigDecimal.ZERO);
            empty.setInvestedAmount(BigDecimal.ZERO);
            return empty;
        });
        BigDecimal rate = currentRate().sellPricePerGram();
        BigDecimal currentValue = holding.getTotalGrams().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gainLoss = currentValue.subtract(holding.getInvestedAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gainLossPct = BigDecimal.ZERO;
        if (holding.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            gainLossPct = gainLoss.multiply(new BigDecimal("100"))
                    .divide(holding.getInvestedAmount(), 2, RoundingMode.HALF_UP);
        }
        return new PortfolioResponse(
                userId,
                holding.getTotalGrams(),
                holding.getInvestedAmount(),
                rate,
                currentValue,
                gainLoss,
                gainLossPct
        );
    }

    private void updateHolding(GoldPurchase purchase) {
        GoldHolding holding = holdingRepository.findByUserId(purchase.getUserId()).orElseGet(() -> {
            GoldHolding newHolding = new GoldHolding();
            newHolding.setUserId(purchase.getUserId());
            newHolding.setTotalGrams(BigDecimal.ZERO);
            newHolding.setInvestedAmount(BigDecimal.ZERO);
            return newHolding;
        });
        holding.setTotalGrams(holding.getTotalGrams().add(purchase.getGramsAllotted()));
        holding.setInvestedAmount(holding.getInvestedAmount().add(purchase.getAmount()));
        holding.setUpdatedAt(Instant.now());
        holdingRepository.save(holding);
    }

    private PurchaseResponse toPurchaseResponse(GoldPurchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUserId(),
                purchase.getAmount(),
                purchase.getRatePerGram(),
                purchase.getGramsAllotted(),
                purchase.getStatus(),
                purchase.getPaymentOrderId(),
                purchase.getPartnerOrderId(),
                purchase.getFailureReason(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    private String requestId() {
        return MDC.get("requestId");
    }
}
