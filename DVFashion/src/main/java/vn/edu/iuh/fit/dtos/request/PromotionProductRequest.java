/*
 * @ {#} PromotionProductRequest.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/*
 * @description: DTO for Promotion Product request
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
public record PromotionProductRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Promotion price is required")
        @DecimalMin(value = "0.0", message = "Promotion price must be >= 0")
        BigDecimal promotionPrice,

        @DecimalMin(value = "0.0", message = "Discount percentage must be >= 0")
        @DecimalMax(value = "100.0", message = "Discount percentage must be <= 100")
        BigDecimal discountPercentage,

        @Min(value = 0, message = "Stock quantity must be >= 0")
        Integer stockQuantity,

        @Min(value = 1, message = "Max quantity per user must be >= 1")
        Integer maxQuantityPerUser,

        Boolean active
) {}
