/*
 * @ {#} ProductStatisticsResponse.java   1.0     11/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.util.Map;

/*
 * @description: DTO for product statistics response
 * @author: Tran Hien Vinh
 * @date:   11/11/2025
 * @version:    1.0
 */
@Builder
public record ProductStatisticsResponse(
        long totalProducts,

        long totalActiveProducts,

        long totalInactiveProducts,

        long totalProductVariants,

        long totalProductsWithLowStock,

        long totalOutOfStockProducts,

        long totalProductsOnPromotion,

        Map<ProductStatus, Long> productsByStatus
) {}