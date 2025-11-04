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
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.User;

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
     * @param user            the user performing the reservation
     * @return true if the reservation is successful, false otherwise
     */
    boolean reserveStock(Long sizeId, int quantity, String referenceNumber, User user);

    /**
     * Release previously reserved stock.
     *
     * @param sizeId          the ID of the product size
     * @param quantity        the quantity to release
     * @param referenceNumber the reference number associated with the reservation
     * @param user            the user performing the release
     */
    void releaseReservedStock(Long sizeId, int quantity, String referenceNumber, User user);

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

    /**
     * Check if a specific quantity of a product size is available
     *
     * @param sizeId   the ID of the product size
     * @param quantity the quantity to check
     * @return true if the quantity is available, false otherwise
     */
    boolean checkAvailability(Long sizeId, int quantity);

    /**
     * Confirm previously reserved stock after order completion
     *
     * @param sizeId          the ID of the product size
     * @param quantity        the quantity to confirm
     * @param referenceNumber the reference number associated with the reservation
     * @param order           the order associated with the stock confirmation
     * @param user            the user performing the confirmation
     */
    void confirmReservedStock(Long sizeId, int quantity, String referenceNumber, User user, Order order);

    /**
     * Release all reserved stock associated with a specific order number
     *
     * @param orderNumber the unique order number
     */
    void releaseReservedStockByOrder(String orderNumber);

    /**
     * Process return stock for a given order
     *
     * @param order the order for which to process return stock
     */
    void processReturnStock(Order order);
}
