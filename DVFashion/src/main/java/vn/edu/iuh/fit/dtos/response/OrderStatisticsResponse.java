/*
 * @ {#} OrderStatisticsResponse.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.OrderStatus;

import java.util.Map;

/*
 * @description: Response DTO for order statistics
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */

@Builder
public record OrderStatisticsResponse(
        long totalOrders,

        Map<OrderStatus, Long> ordersByStatus
) {}
