/*
 * @ {#} PayPalServiceImpl1.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.config.PayPalClient;
import vn.edu.iuh.fit.dtos.response.PayPalCreateResponse;
import vn.edu.iuh.fit.exceptions.PaypalException;
import vn.edu.iuh.fit.services.PayPalService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/*
 * @description: Service implementation for PayPal payment processing
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalServiceImpl implements PayPalService {
    private final PayPalClient payPalClient;

    private final RestTemplate restTemplate;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Override
    public PayPalCreateResponse createPayment(BigDecimal total, String orderNumber) {
        // Get access token from PayPal
        String accessToken = payPalClient.getAccessToken();

        // Create request body — content to send to PayPal
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE", // Payment intent
                "purchase_units", List.of(Map.of( // List of purchase units (reference_id, amount)
                        "reference_id", orderNumber, // Reference ID for the order
                        "amount", Map.of( // Amount details (currency, value)
                                "currency_code", "USD", // Currency code
                                "value", total.setScale(2, RoundingMode.HALF_UP).toString() // "59.99"
                        )
                )),
                "application_context", Map.of( // URL to redirect after payment approval/cancellation
                        "return_url", successUrl + "?orderNumber=" + orderNumber,
                        "cancel_url", cancelUrl + "?orderNumber=" + orderNumber
                )
        );

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP entity with body and headers
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Send POST request to PayPal to create order
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        // Extract id, approval URL from response
        Map<String, Object> responseBody = response.getBody();

        // Validate response body
        if (responseBody == null) {
            throw new PaypalException("Invalid response from PayPal API: body is null");
        }

        // Validate response body contains 'id'
        if (!responseBody.containsKey("id")) {
            throw new PaypalException("Invalid response from PayPal API: missing 'id'");
        }

        // Validate response body contains 'links'
        if (!responseBody.containsKey("links")) {
            throw new PaypalException("Invalid response from PayPal API: missing 'links'");
        }

        // Get order ID and approval URL
        String payPalOrderId = responseBody.get("id").toString();
        List<Map<String, Object>> links = (List<Map<String, Object>>) responseBody.get("links");

        String approvalUrl= links.stream()
                .filter(link -> "approve".equals(link.get("rel")))
                .findFirst()
                .map(link -> link.get("href").toString())
                .orElseThrow(() -> new RuntimeException("Approval URL not found"));

        return new PayPalCreateResponse(payPalOrderId, approvalUrl);
    }

    @Override
    public void capturePayment(String orderId) {
        // Get access token from PayPal
        String accessToken = payPalClient.getAccessToken();

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP entity with headers (no body needed for capture)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Send POST request to PayPal to capture payment
        // This is the official PayPal API for “closing the deal”
        restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                entity,
                Map.class
        );
    }
}
