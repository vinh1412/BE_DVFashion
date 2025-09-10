/*
 * @ {#} AddToCartRequest.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * @description: DTO for adding a product variant to the shopping cart
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public record AddToCartRequest(
     @NotNull(message = "Product variant ID must not be null")
     Long productVariantId,

     @NotNull(message = "Size ID is required")
     Long sizeId,

     @Min(value = 1, message = "Quantity must be at least 1")
     Integer quantity
) {}
