/*
 * @ {#} TopRecommendedProductResponse.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

/*
 * @description: DTO representing the top recommended product response.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Builder
public record TopRecommendedProductResponse(
        Long productId,

        String productName,

        String categoryName,

        Long recommendationCount,

        Double averagePrice
) {}
