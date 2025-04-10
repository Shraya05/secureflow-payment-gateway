package com.secureflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureflow.config.PayPalConfig;
import com.secureflow.exception.PayPalServiceException;
import com.secureflow.model.PaymentEntity;
import com.secureflow.model.User;
import com.secureflow.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayPalConfig payPalConfig;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // for JSON
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public String createPayment(BigDecimal total, String currency, String description,
                                String cancelUrl, String successUrl) {
        try {
            log.debug("Creating PayPal payment via REST API");

            Map<String, Object> payload = buildPaymentPayload(total, currency, description, cancelUrl, successUrl);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, payPalConfig.getAuthHeaders());

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    payPalConfig.getBaseUrl() + "/v1/payments/payment",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                String paymentId = (String) response.getBody().get("id");
                String state = (String) response.getBody().get("state");

                savePaymentRecord(paymentId, state, total, currency);
                log.info("Payment created with ID: {}", paymentId);

                // Extract approval link
                List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
                return links.stream()
                        .filter(link -> "approval_url".equals(link.get("rel")))
                        .map(link -> link.get("href"))
                        .findFirst()
                        .orElseThrow(() -> new PayPalServiceException("Approval URL not found"));
            } else {
                throw new PayPalServiceException("Failed to create PayPal payment");
            }

        } catch (Exception e) {
            log.error("PayPal payment creation failed: {}", e.getMessage());
            throw new PayPalServiceException("PayPal payment creation error", e);
        }
    }

    private Map<String, Object> buildPaymentPayload(BigDecimal total, String currency, String description,
                                                    String cancelUrl, String successUrl) {
        Map<String, Object> amount = Map.of(
                "currency", currency,
                "total", String.format("%.2f", total)
        );

        Map<String, Object> transaction = Map.of(
                "amount", amount,
                "description", description
        );

        Map<String, Object> payer = Map.of("payment_method", "paypal");
        Map<String, Object> redirectUrls = Map.of(
                "cancel_url", cancelUrl,
                "return_url", successUrl
        );

        return Map.of(
                "intent", "sale",
                "payer", payer,
                "transactions", List.of(transaction),
                "redirect_urls", redirectUrls
        );
    }

    private void savePaymentRecord(String paymentId, String status, BigDecimal amount, String currency) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentId(paymentId);
        entity.setStatus(status);
        entity.setAmount(amount);
        entity.setCurrency(currency);
        entity.setUser(user);

        paymentRepository.save(entity);
        log.debug("Saved payment record with ID: {}", entity.getId());
    }

    @Transactional
    public void executePayment(String paymentId, String payerId) {
        try {
            log.debug("Executing PayPal payment with ID: {}", paymentId);

            Map<String, String> payload = Map.of("payer_id", payerId);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, payPalConfig.getAuthHeaders());

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    payPalConfig.getBaseUrl() + "/v1/payments/payment/" + paymentId + "/execute",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String state = (String) response.getBody().get("state");
                updatePaymentStatus(paymentId, state);
                log.info("Payment executed. State: {}", state);
            } else {
                throw new PayPalServiceException("Payment execution failed with status " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Payment execution failed: {}", e.getMessage());
            throw new PayPalServiceException("Error executing payment", e);
        }
    }

    private void updatePaymentStatus(String paymentId, String status) {
        paymentRepository.findByPaymentId(paymentId)
                .ifPresent(entity -> {
                    entity.setStatus(status);
                    paymentRepository.save(entity);
                    log.info("Updated payment {} status to {}", paymentId, status);
                });
    }

    public boolean isPaymentCompleted(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .map(payment -> "approved".equalsIgnoreCase(payment.getStatus()))
                .orElse(false);
    }

    public void cancelPayment(String paymentId) {
        log.warn("PayPal REST API does not support cancelling a payment explicitly. Payment ID: {}", paymentId);
    }

    public Map<String, Object> getPaymentDetails(String paymentId) {
        try {
            HttpEntity<Void> request = new HttpEntity<>(payPalConfig.getAuthHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    payPalConfig.getBaseUrl() + "/v1/payments/payment/" + paymentId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to retrieve payment details: {}", e.getMessage());
            throw new PayPalServiceException("Could not fetch payment details", e);
        }
    }
}
