/*
 * @ {#} CategoryStatisticsResponse.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.util.Map;

/*
 * @description: DTO for category statistics response
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Builder
public record CategoryStatisticsResponse(
        long totalCategories,

        long totalActiveCategories,

        long totalInactiveCategories,

        long totalCategoriesWithProducts,

        long totalCategoriesWithActiveProducts,

        Map<Boolean, Long> categoriesByActiveStatus
) {}
