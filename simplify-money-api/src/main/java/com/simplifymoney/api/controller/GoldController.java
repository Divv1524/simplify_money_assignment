package com.simplifymoney.api.controller;

import com.simplifymoney.api.dto.GoldRateResponse;
import com.simplifymoney.api.dto.GoldHoldingResponse;
import com.simplifymoney.api.dto.InitiatePurchaseRequest;
import com.simplifymoney.api.dto.InitiatePurchaseResponse;
import com.simplifymoney.api.dto.PayPurchaseRequest;
import com.simplifymoney.api.dto.PortfolioResponse;
import com.simplifymoney.api.dto.PurchaseResponse;
import com.simplifymoney.api.service.GoldPurchaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GoldController {
    private final GoldPurchaseService goldPurchaseService;

    public GoldController(GoldPurchaseService goldPurchaseService) {
        this.goldPurchaseService = goldPurchaseService;
    }

    @GetMapping("/gold/rate")
    public GoldRateResponse currentRate() {
        return goldPurchaseService.currentRate();
    }

    @PostMapping("/gold/purchase/initiate")
    public InitiatePurchaseResponse initiate(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody InitiatePurchaseRequest request
    ) {
        return goldPurchaseService.initiate(request, idempotencyKey);
    }

    @PostMapping("/gold/purchase/{purchaseId}/pay")
    public PurchaseResponse pay(@PathVariable String purchaseId, @Valid @RequestBody PayPurchaseRequest request) {
        return goldPurchaseService.pay(purchaseId, request);
    }

    @GetMapping("/gold/purchases/{purchaseId}")
    public PurchaseResponse purchase(@PathVariable String purchaseId) {
        return goldPurchaseService.getPurchase(purchaseId);
    }

    @GetMapping("/gold/purchases/user/{userId}")
    public List<PurchaseResponse> purchasesByUser(@PathVariable String userId) {
        return goldPurchaseService.purchasesByUser(userId);
    }

    @GetMapping("/gold/holdings/{userId}")
    public GoldHoldingResponse holding(@PathVariable String userId) {
        return goldPurchaseService.holding(userId);
    }

    @PostMapping("/gold/purchase/{purchaseId}/cancel")
    public PurchaseResponse cancel(@PathVariable String purchaseId) {
        return goldPurchaseService.cancel(purchaseId);
    }

    @GetMapping("/portfolio/{userId}")
    public PortfolioResponse portfolio(@PathVariable String userId) {
        return goldPurchaseService.portfolio(userId);
    }
}
