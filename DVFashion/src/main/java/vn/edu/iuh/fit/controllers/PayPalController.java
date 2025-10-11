/*
 * @ {#} PayPalController.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.services.OrderService;

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

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<OrderResponse>> success(
            @RequestParam("token") String token,
            @RequestParam("orderNumber") String orderNumber
    ) {
        OrderResponse response = orderService.confirmPayPalPayment(token, orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment successful and order confirmed."));
    }

    @GetMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancel(@RequestParam("orderNumber") String orderNumber) {
        String message = orderService.cancelPayPalPayment(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
