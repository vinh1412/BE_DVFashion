/*
 * @ {#} OrderItemRequest.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record OrderItemRequest(
        @NotNull(message = "Product variant ID is required")
        Long productVariantId,

        @NotNull(message = "Size ID is required")
        Long sizeId,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        BigDecimal discount
) {
}
