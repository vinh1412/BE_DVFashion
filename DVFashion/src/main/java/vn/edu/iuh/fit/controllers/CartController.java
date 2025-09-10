/*
 * @ {#} CartController.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.AddToCartRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.CartResponse;
import vn.edu.iuh.fit.services.CartService;

/*
 * @description: Controller for managing shopping cart operations
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addToCart(@Valid @RequestBody AddToCartRequest request) {
            CartResponse response = cartService.addToCart(request);

            return ResponseEntity.ok(ApiResponse.success(response, "Product added to cart successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCart() {
        CartResponse response = cartService.getCart();

        return ResponseEntity.ok(ApiResponse.success(response, "Cart retrieved successfully."));
    }
}
