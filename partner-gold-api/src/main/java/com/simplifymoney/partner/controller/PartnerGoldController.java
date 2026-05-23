package com.simplifymoney.partner.controller;

import com.simplifymoney.partner.dto.GoldAllotmentRequest;
import com.simplifymoney.partner.dto.GoldAllotmentResponse;
import com.simplifymoney.partner.dto.GoldRateResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/partner/gold")
public class PartnerGoldController {
    private static final Logger log = LoggerFactory.getLogger(PartnerGoldController.class);
    private static final BigDecimal BUY_PRICE = new BigDecimal("7200.50");
    private static final BigDecimal SELL_PRICE = new BigDecimal("7100.25");

    @GetMapping("/rate")
    public GoldRateResponse currentRate(@RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        log.info("Returning mocked current gold rate");
        return new GoldRateResponse("GOLD", BUY_PRICE, SELL_PRICE, Instant.now().plusSeconds(60));
    }

    @PostMapping("/allot")
    public GoldAllotmentResponse allotGold(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody GoldAllotmentRequest request
    ) {
        BigDecimal grams = request.amount().divide(request.ratePerGram(), 8, RoundingMode.HALF_UP);
        String partnerOrderId = "PGO_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.info("Gold allotted partnerOrderId={} merchantPurchaseId={} userId={} grams={}",
                partnerOrderId, request.merchantPurchaseId(), request.userId(), grams);
        return new GoldAllotmentResponse(
                partnerOrderId,
                request.merchantPurchaseId(),
                request.amount(),
                request.ratePerGram(),
                grams,
                "ALLOCATED",
                Instant.now()
        );
    }
}
