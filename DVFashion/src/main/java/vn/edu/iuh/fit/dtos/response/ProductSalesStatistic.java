/*
 * @ {#} ProductSalesStatistic.java   1.0     11/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;

/*
 * @description: DTO for product sales statistics
 * @author: Tran Hien Vinh
 * @date:   11/11/2025
 * @version:    1.0
 */
@Builder
public record ProductSalesStatistic(
        Long productId,

        String productName,

        Long totalQuantitySold,

        BigDecimal totalRevenue
) {}
