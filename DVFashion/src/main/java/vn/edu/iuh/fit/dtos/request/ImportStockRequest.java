/*
 * @ {#} ImportStockRequest.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * @description: Request DTO for importing stock
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record ImportStockRequest(
        @NotNull(message = "Size ID cannot be null")
        Long sizeId,

        @Min(value = 1, message = "Import quantity must be at least 1")
        Integer quantity,

        String notes,

        String supplierInfo
) {}
