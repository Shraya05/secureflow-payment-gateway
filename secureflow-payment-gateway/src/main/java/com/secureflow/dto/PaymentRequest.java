package com.secureflow.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String description;
    private String cancelUrl;
    private String successUrl;
}