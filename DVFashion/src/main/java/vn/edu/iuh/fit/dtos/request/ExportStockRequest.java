/*
 * @ {#} ExportStockRequest.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * @description: Request DTO for exporting stock
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record ExportStockRequest(
        @NotNull(message = "Size ID cannot be null")
        Long sizeId,

        @NotNull(message = "Export quantity cannot be null")
        @Min(value = 1, message = "Export quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "Reason for export cannot be null")
        String reason,

        String notes
) {}

