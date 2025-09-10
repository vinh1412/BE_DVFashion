/*
 * @ {#} InventoryService.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

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
}
