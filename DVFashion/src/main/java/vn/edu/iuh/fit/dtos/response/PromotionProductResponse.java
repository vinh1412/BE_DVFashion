/*
 * @ {#} PromotionProductResponse.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;

/*
 * @description: DTO for Promotion Product response
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
public record PromotionProductResponse(
        Long id,

        Long productId,

        String productName,

        BigDecimal originalPrice,

        BigDecimal promotionPrice,

        BigDecimal discountPercentage,

        Integer stockQuantity,

        int soldQuantity,

        Integer maxQuantityPerUser,

        boolean active
) {}
