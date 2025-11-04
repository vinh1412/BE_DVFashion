/*
 * @ {#} UpdatePromotionProductRequest.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/*
 * @description: Request DTO for updating a promotion product
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
public record UpdatePromotionProductRequest(
        Long id, // ID PromotionProduct (null if new product to be added)

        @Positive(message = "Product ID must be positive")
        Long productId,

        @DecimalMin(value = "0.0", message = "Promotion price must be at least 0")
        @Digits(integer = 10, fraction = 2, message = "Invalid promotion price format")
        BigDecimal promotionPrice,

        @DecimalMin(value = "0.0", message = "Discount percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
        @Digits(integer = 3, fraction = 2, message = "Invalid discount percentage format")
        BigDecimal discountPercentage,

        @Min(value = 0, message = "Stock quantity must be at least 0")
        Integer stockQuantity,

        @Min(value = 1, message = "Max quantity per user must be at least 1")
        Integer maxQuantityPerUser,

        Boolean active
) {}
