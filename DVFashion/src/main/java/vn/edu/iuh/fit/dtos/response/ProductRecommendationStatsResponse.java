/*
 * @ {#} ProductRecommendationStatsResponse.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

/*
 * @description: Response DTO for product recommendation statistics
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Builder
public record ProductRecommendationStatsResponse(
        Long productId,

        String productName,

        String categoryName,

        Long recommendationCount,

        Long clickCount,

        Long addToCartCount,

        Long purchaseCount,

        Double clickThroughRate,

        Double cartConversionRate,

        Double purchaseConversionRate
) {}
