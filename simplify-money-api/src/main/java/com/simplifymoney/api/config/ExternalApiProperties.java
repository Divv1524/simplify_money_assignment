package com.simplifymoney.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external")
public record ExternalApiProperties(String partnerGoldBaseUrl, String paymentGatewayBaseUrl) {
}
