/*
 * @ {#} InventoryStatsResponse.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: Response DTO for inventory statistics
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record InventoryStatsResponse(
        Long totalItems,

        Long totalStockQuantity,

        Long totalReservedQuantity,

        Long totalAvailableQuantity,

        Long lowStockItemsCount,

        Long outOfStockItemsCount,

        Double lowStockPercentage,

        Double outOfStockPercentage
) {}
