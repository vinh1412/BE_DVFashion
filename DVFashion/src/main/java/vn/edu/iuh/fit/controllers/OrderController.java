/*
 * @ {#} OrderController.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.services.OrderService;

/*
 * @description: Controller for handling order-related endpoints
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success(orderResponse));
    }
}
