package com.secureflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.api.base-url}")
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    private String accessToken;

    public HttpHeaders getAuthHeaders() {
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = getAccessToken();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }
    /**
     * Retrieves an OAuth 2.0 access token from PayPal
     */
    public String getAccessToken() {
        validateCredentials();

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        try {
            ResponseEntity<Map> response = restTemplate().postForEntity(
                    baseUrl + "/v1/oauth2/token", request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                log.info("PayPal access token retrieved successfully.");
                return accessToken;
            } else {
                throw new RuntimeException("Failed to retrieve access token: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error while getting PayPal access token: {}", e.getMessage());
            throw new RuntimeException("PayPal access token fetch failed", e);
        }
    }

    /**
     * Helper method to validate required credentials
     */
    private void validateCredentials() {
        if (Objects.isNull(clientId) || clientId.isBlank()) {
            throw new IllegalStateException("PayPal client ID is not configured");
        }
        if (Objects.isNull(clientSecret) || clientSecret.isBlank()) {
            throw new IllegalStateException("PayPal client secret is not configured");
        }
    }

    /**
     * Public getter for the PayPal API base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Builds the webhook verification URL
     */
    public String getWebhookVerifyUrl() {
        return baseUrl + "/v1/notifications/verify-webhook-signature";
    }
}
