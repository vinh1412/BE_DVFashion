/*
 * @ {#} InventoryService.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ExportStockRequest;
import vn.edu.iuh.fit.dtos.request.ImportStockRequest;
import vn.edu.iuh.fit.dtos.request.StockAdjustmentRequest;
import vn.edu.iuh.fit.dtos.response.InventoryResponse;
import vn.edu.iuh.fit.dtos.response.InventoryStatsResponse;

import java.util.List;

/*
 * @description: Service interface for inventory management
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public interface InventoryService {
    /**
     * Reserve stock for a specific product size.
     *
     * @param sizeId          the ID of the product size
     * @param quantity        the quantity to reserve
     * @param referenceNumber a unique reference number for the reservation
     * @return true if the reservation is successful, false otherwise
     */
    boolean reserveStock(Long sizeId, int quantity, String referenceNumber);

    /**
     * Release previously reserved stock.
     *
     * @param sizeId          the ID of the product size
     * @param quantity        the quantity to release
     * @param referenceNumber the reference number associated with the reservation
     */
    void releaseReservedStock(Long sizeId, int quantity, String referenceNumber);

    /**
     * Get the available quantity of a specific product size.
     *
     * @param sizeId the ID of the product size
     * @return the available quantity
     */
    int getAvailableQuantity(Long sizeId);


    /**
     * Import stock into inventory
     *
     * @param request the import stock request containing sizeId and quantity
     * @return the updated inventory response
     */
    InventoryResponse importStock(ImportStockRequest request);

    /**
     * Export stock from inventory
     *
     * @param request the export stock request containing sizeId and quantity
     * @return the updated inventory response
     */
    InventoryResponse exportStock(ExportStockRequest request);

    /**
     * Adjust stock levels in inventory
     *
     * @param request the stock adjustment request containing sizeId, adjustmentType, and quantity
     * @return the updated inventory response
     */
    InventoryResponse adjustStock(StockAdjustmentRequest request);

    /**
     * Get a report of all inventory items
     *
     * @return a list of inventory responses
     */
    List<InventoryResponse> getInventoryReport();

    /**
     * Get inventory details by size ID
     *
     * @param sizeId the ID of the product size
     * @return the inventory response
     */
    InventoryResponse getInventoryBySize(Long sizeId);

    /**
     * Get inventory statistics
     *
     * @return the inventory statistics response
     */
    InventoryStatsResponse getInventoryStats();

    /**
     * Get a list of items that are low in stock
     *
     * @return a list of inventory responses for low stock items
     */
    List<InventoryResponse> getLowStockItems();

    /**
     * Get a list of items that are out of stock
     *
     * @return a list of inventory responses for out of stock items
     */
    List<InventoryResponse> getOutOfStockItems();
}
