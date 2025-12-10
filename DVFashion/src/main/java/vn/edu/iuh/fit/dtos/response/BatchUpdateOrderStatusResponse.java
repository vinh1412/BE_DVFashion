/*
 * @ {#} BatchUpdateOrderStatusResponse.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.OrderStatus;

import java.util.List;

/*
 * @description: Response DTO for batch updating order statuses
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Builder
public record BatchUpdateOrderStatusResponse(
        int totalOrders,

        int successfulUpdates,

        int failedUpdates,

        List<OrderUpdateResult> results
) {

    @Builder
    public record OrderUpdateResult(
            String orderNumber,

            boolean success,

            OrderStatus oldStatus,

            OrderStatus newStatus,

            String errorMessage
    ) {}
}
