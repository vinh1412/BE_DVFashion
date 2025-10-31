/*
 * @ {#} CreateOrderRequest.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/*
 * @description: DTO for creating a new order
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record CreateOrderRequest(
        @Valid
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> orderItems,

        @Valid
        ShippingInfoRequest shippingInfo,

        String notes,

        @NotNull(message = "Payment method is required")
        @Pattern(regexp = "CASH_ON_DELIVERY|PAYPAL|BANK_TRANSFER", message = "Invalid payment method")
        String paymentMethod,

        Long promotionId
) {}
