/*
 * @ {#} StockAdjustmentRequest.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * @description: Request DTO for adjusting stock levels
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record StockAdjustmentRequest(
        @NotNull(message = "Size ID cannot be null")
        Long sizeId,

        @NotNull(message = "New quantity cannot be null")
        @Min(value = 0, message = "New quantity must be zero or greater")
        Integer newQuantity,

        @NotNull(message = "Reason for adjustment cannot be null")
        String reason,

        String notes
) {}
