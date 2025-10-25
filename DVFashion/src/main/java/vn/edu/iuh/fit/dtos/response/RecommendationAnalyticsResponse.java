/*
 * @ {#} RecommendationAnalyticsResponse.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

/*
 * @description: DTO for recommendation analytics response
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Builder
public record RecommendationAnalyticsResponse(
        Long totalRecommendations,

        Long totalClicks,

        Long totalAddToCarts,

        Long totalPurchases,

        Double clickThroughRate,

        Double cartConversionRate,

        Double purchaseConversionRate,

        String period
) {}
