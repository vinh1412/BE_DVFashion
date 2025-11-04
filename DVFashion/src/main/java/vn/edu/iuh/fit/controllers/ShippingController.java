/*
 * @ {#} ShippingController.java   1.0     29/10/2025
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
import vn.edu.iuh.fit.dtos.response.ShippingCalculationResponse;
import vn.edu.iuh.fit.services.ShippingService;

/*
 * @description: Controller for handling shipping-related endpoints
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${web.base-path}/shipping")
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ShippingCalculationResponse>> calculateShipping(
            @Valid @RequestBody CreateOrderRequest request) {
        ShippingCalculationResponse response = shippingService.calculateShipping(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Shipping calculated successfully"));
    }
}
