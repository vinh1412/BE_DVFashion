/*
 * @ {#} CreateOrderRequest.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import vn.edu.iuh.fit.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for creating a new order
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record CreateOrderRequest(
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotEmpty(message = "Cart item IDs cannot be empty")
        List<Long> cartItemIds,

        @Valid
        ShippingInfoRequest shippingInfo,

        Long promotionId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String notes,

        BigDecimal shippingFee
        ) {
}
