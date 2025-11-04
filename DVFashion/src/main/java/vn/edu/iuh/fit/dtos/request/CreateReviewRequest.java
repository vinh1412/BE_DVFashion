/*
 * @ {#} CreateReviewRequest.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.*;

/*
 * @description: DTO class for creating a new product review request.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public record CreateReviewRequest(
        @NotNull(message = "Order ID is required")
        @Positive(message = "Order ID must be positive")
        Long orderId,

        @NotNull(message = "Product variant ID is required")
        @Positive(message = "Product variant ID must be positive")
        Long productVariantId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment
) {}
