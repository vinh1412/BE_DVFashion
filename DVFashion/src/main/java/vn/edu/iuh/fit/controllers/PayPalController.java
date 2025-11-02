/*
 * @ {#} PayPalController.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.services.OrderService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
 * @description: Controller for handling PayPal payment callbacks
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${web.base-path}/payments/paypal")
@Slf4j
public class PayPalController {
    private final OrderService orderService;

    @Value("${web.frontend-base-url}")
    private String frontendBaseUrl;

    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam("token") String token,
            @RequestParam("orderNumber") String orderNumber
    ) {
        // Option A: nếu bạn muốn backend confirm trước rồi redirect to frontend order-success:
        try {
            OrderResponse response = orderService.confirmPayPalPayment(token, orderNumber);
            // redirect to frontend order success page
            String redirectUrl = String.format("%s/order-success/%s", frontendBaseUrl, response.orderNumber());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (Exception e) {
            log.error("PayPal confirm failed: {}", e.getMessage(), e);
            // on error redirect to frontend cart or error page
            String redirectUrl = String.format("%s/cart", frontendBaseUrl);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        }

        // Option B (alternative): chỉ redirect token+orderNumber to frontend and let frontend call confirm
        // String redirectUrl = String.format("%s/payment/paypal/success?token=%s&orderNumber=%s",
        //        frontendBaseUrl, URLEncoder.encode(token, StandardCharsets.UTF_8), URLEncoder.encode(orderNumber, StandardCharsets.UTF_8));
        // return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam("orderNumber") String orderNumber) {
        // you can call orderService.cancelPayPalPayment(orderNumber) or simply redirect to frontend cancel handler
        try {
            orderService.cancelPayPalPayment(orderNumber);
        } catch (Exception e) {
            log.warn("Cancel PayPal failed: {}", e.getMessage());
        }
        String redirectUrl = String.format("%s/payment/paypal/cancel?orderNumber=%s", frontendBaseUrl, URLEncoder.encode(orderNumber, StandardCharsets.UTF_8));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }
}
