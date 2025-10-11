/*
 * @ {#} PayPalClient.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

/*
 * @description: Client for interacting with PayPal API
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalClient {
    private final RestTemplate restTemplate;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    private String cachedAccessToken;

    private Instant tokenExpiryTime;

    /**
     * Get a valid PayPal access token. Automatically refreshes when expired.
     */
    public synchronized String getAccessToken() {
        // If we have a cached token and it's still valid, return it
        if (cachedAccessToken != null && tokenExpiryTime != null && Instant.now().isBefore(tokenExpiryTime)) {
            log.debug("Using cached PayPal token (expires at: {})", tokenExpiryTime);
            return cachedAccessToken;
        }

        log.info("Requesting new PayPal access token...");

        try {
            // Prepare headers with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret); // = Authorization: Basic <base64(clientId:clientSecret)>

            // Prepare request entity
            HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

            // Make the POST request to get the token
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v1/oauth2/token",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();


            // Validate response
            if (body == null || !body.containsKey("access_token")) {
                throw new IllegalStateException("PayPal response missing access_token");
            }

            // Cache the token and its expiry time
            cachedAccessToken = (String) body.get("access_token");

            // expires_in is in seconds
            int expiresIn = ((Number) body.get("expires_in")).intValue();

            // Set expiry time a bit earlier to avoid edge cases
            tokenExpiryTime = Instant.now().plusSeconds(expiresIn - 60); // -60s buffer

            log.info("New PayPal token acquired (expires in {}s)", expiresIn);
            return cachedAccessToken;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("PayPal token error: status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to get PayPal access token: " + ex.getMessage(), ex);

        } catch (Exception e) {
            log.error("Unexpected error while getting PayPal token", e);
            throw new RuntimeException("Unexpected error while getting PayPal token", e);
        }
    }
}
