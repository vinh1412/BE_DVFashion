/*
 * @ {#} InventoryResponse.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for inventory details
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record InventoryResponse(
        Long id,

        Long sizeId,

        Long productId,

        String sizeName,

        String productName,

        String productColor,

        int quantityInStock,

        int reservedQuantity,

        int availableQuantity,

        int minStockLevel,

        LocalDateTime lastUpdated,

        boolean isLowStock
) {}
