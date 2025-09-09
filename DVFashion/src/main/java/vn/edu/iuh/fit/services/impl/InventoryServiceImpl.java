/*
 * @ {#} InventoryServiceImpl.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Inventory;
import vn.edu.iuh.fit.entities.StockTransaction;
import vn.edu.iuh.fit.enums.StockTransactionType;
import vn.edu.iuh.fit.repositories.InventoryRepository;
import vn.edu.iuh.fit.repositories.StockTransactionRepository;
import vn.edu.iuh.fit.services.InventoryService;

import java.util.Optional;

/*
 * @description: Implementation of InventoryService for managing inventory operations
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    private final StockTransactionRepository stockTransactionRepository;

    @Override
    public boolean reserveStock(Long sizeId, int quantity, String referenceNumber) {
        // Find inventory with pessimistic lock
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeIdWithLock(sizeId);

        // If inventory not found
        if (inventoryOpt.isEmpty()) {
            log.warn("Inventory not found for size ID: {}", sizeId);
            return false;
        }

        //Get inventory
        Inventory inventory = inventoryOpt.get();

        // If quantity requested exceeds available stock
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("Insufficient stock. Available: {}, Requested: {}",
                    inventory.getAvailableQuantity(), quantity);
            return false;
        }

        // Reserve stock by updating reserved quantity
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);

        // Create and save stock transaction log
        StockTransaction transaction = StockTransaction.builder()
                .inventory(inventory)
                .transactionType(StockTransactionType.RESERVE)
                .quantity(quantity)
                .referenceNumber(referenceNumber)
                .notes("Reserved for cart item")
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Reserved {} items for size ID: {}, reference: {}",
                quantity, sizeId, referenceNumber);
        return true;
    }

    @Override
    public void releaseReservedStock(Long sizeId, int quantity, String referenceNumber) {
        // Find inventory with pessimistic lock
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeIdWithLock(sizeId);

        // If inventory found, release reserved stock
        if (inventoryOpt.isPresent()) {

            Inventory inventory = inventoryOpt.get();

            // Update reserved quantity, ensuring it doesn't go below zero
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
            inventoryRepository.save(inventory);

            // Create and save stock transaction log
            StockTransaction transaction = StockTransaction.builder()
                    .inventory(inventory)
                    .transactionType(StockTransactionType.RELEASE)
                    .quantity(quantity)
                    .referenceNumber(referenceNumber)
                    .notes("Released from cart item")
                    .build();

            stockTransactionRepository.save(transaction);

            log.info("Released {} items for size ID: {}, reference: {}",
                    quantity, sizeId, referenceNumber);
        }
    }

    @Override
    public int getAvailableQuantity(Long sizeId) {
        // Get available quantity for the given size ID
        return inventoryRepository.findBySizeId(sizeId)
                .map(Inventory::getAvailableQuantity)
                .orElse(0);
    }
}
