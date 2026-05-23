package com.simplifymoney.partner.controller;

import com.simplifymoney.partner.dto.GoldAllotmentRequest;
import com.simplifymoney.partner.dto.GoldAllotmentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerGoldControllerTest {
    @Test
    void allotGoldCalculatesGramsFromAmountAndRate() {
        PartnerGoldController controller = new PartnerGoldController();

        GoldAllotmentResponse response = controller.allotGold(
                "test-request",
                new GoldAllotmentRequest("USER_1", "PURCHASE_1", BigDecimal.TEN, new BigDecimal("7200.50"))
        );

        assertThat(response.status()).isEqualTo("ALLOCATED");
        assertThat(response.gramsAllotted()).isEqualByComparingTo("0.00138879");
    }
}
