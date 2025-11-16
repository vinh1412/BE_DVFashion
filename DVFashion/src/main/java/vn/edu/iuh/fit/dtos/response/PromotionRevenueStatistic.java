/*
 * @ {#} PromotionRevenueStatistic.java   1.0     11/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.dtos.response;
/*
 * @description: DTO for promotion revenue statistics
 * @author: Tran Hien Vinh
 * @date:   11/11/2025
 * @version:    1.0
 */
public record PromotionRevenueStatistic(
        Long promotionId,

        String promotionName,

        Double totalRevenue
) {}