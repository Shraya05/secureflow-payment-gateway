package com.secureflow.controller;

import com.secureflow.dto.PaymentRequest;
import com.secureflow.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // Create Payment using PaymentRequest DTO
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            String approvalLink = paymentService.createPayment(
                    paymentRequest.getAmount(),
                    paymentRequest.getCurrency(),
                    paymentRequest.getDescription(),
                    paymentRequest.getCancelUrl(),
                    paymentRequest.getSuccessUrl()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "created",
                    "approval_url", approvalLink
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // Success callback
    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            paymentService.executePayment(paymentId, payerId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment executed successfully",
                    "paymentId", paymentId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // Cancel callback
    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancel() {
        return ResponseEntity.ok(Map.of(
                "status", "cancelled",
                "message", "Payment was cancelled by user"
        ));
    }
}
