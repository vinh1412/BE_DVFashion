/*
 * @ {#} BatchUpdateOrderStatusRequest.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

/*
 * @description: Request DTO for batch updating order statuses
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Builder
public record BatchUpdateOrderStatusRequest(
        @NotEmpty(message = "Order numbers cannot be empty")
        @Size(max = 50, message = "Cannot update more than 50 orders at once")
        List<String> orderNumbers,

        @NotNull(message = "Target status is required")
        String targetStatus,

        String notes
) {}
