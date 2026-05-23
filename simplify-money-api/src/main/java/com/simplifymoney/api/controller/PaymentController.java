package com.simplifymoney.api.controller;

import com.simplifymoney.api.dto.PaymentMethodResponse;
import com.simplifymoney.api.service.GoldPurchaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final GoldPurchaseService goldPurchaseService;

    public PaymentController(GoldPurchaseService goldPurchaseService) {
        this.goldPurchaseService = goldPurchaseService;
    }

    @GetMapping("/payment-methods")
    public List<PaymentMethodResponse> paymentMethods() {
        return goldPurchaseService.paymentMethods();
    }
}
