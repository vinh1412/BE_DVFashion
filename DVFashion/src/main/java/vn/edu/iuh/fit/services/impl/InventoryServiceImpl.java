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
import vn.edu.iuh.fit.dtos.request.ExportStockRequest;
import vn.edu.iuh.fit.dtos.request.ImportStockRequest;
import vn.edu.iuh.fit.dtos.request.StockAdjustmentRequest;
import vn.edu.iuh.fit.dtos.response.InventoryResponse;
import vn.edu.iuh.fit.dtos.response.InventoryStatsResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.StockTransactionType;
import vn.edu.iuh.fit.exceptions.InsufficientStockException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.InventoryMapper;
import vn.edu.iuh.fit.repositories.InventoryRepository;
import vn.edu.iuh.fit.repositories.SizeRepository;
import vn.edu.iuh.fit.repositories.StockTransactionRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.InventoryService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.util.List;
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

    private final InventoryMapper inventoryMapper;

    private final UserService userService;

    private final UserRepository userRepository;

    private final SizeRepository sizeRepository;

    @Override
    public boolean reserveStock(Long sizeId, int quantity, String referenceNumber, User user) {
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
                .createdBy(user)
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Reserved {} items for size ID: {}, reference: {}",
                quantity, sizeId, referenceNumber);
        return true;
    }

    @Override
    public void releaseReservedStock(Long sizeId, int quantity, String referenceNumber, User user) {
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
                    .createdBy(user)
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

    @Override
    public InventoryResponse importStock(ImportStockRequest request) {
        // Validate size exists
        Size size = sizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Size not found "+ request.sizeId()));

        // Get current user
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found "+ currentUserResponse.getId()));

        // Get or create inventory with lock
        Inventory inventory = inventoryRepository.findBySizeIdWithLock(request.sizeId())
                .orElseGet(() -> createInventoryForSize(size));

        // Generate reference number
        String referenceNumber = "IMPORT_" + request.sizeId() + "_" + System.currentTimeMillis();

        // Update stock quantity
        int oldQuantity = inventory.getQuantityInStock();
        inventory.setQuantityInStock(oldQuantity + request.quantity());


        // Save changes
        inventory = inventoryRepository.save(inventory);

        // Create transaction record
        StockTransaction transaction = StockTransaction.builder()
                .inventory(inventory)
                .transactionType(StockTransactionType.INBOUND)
                .quantity(request.quantity())
                .referenceNumber(referenceNumber)
                .notes(buildImportNotes(request))
                .createdBy(user)
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Imported {} units for size {} (old: {}, new: {})",
                request.quantity(), request.sizeId(), oldQuantity, inventory.getQuantityInStock());

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return inventoryMapper.mapToInventoryResponse(inventory, currentLanguage);
    }

    @Override
    public InventoryResponse exportStock(ExportStockRequest request) {
        // Validate size exists
        sizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Size not found"));

        // Get current user
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Get inventory with lock
        Inventory inventory = inventoryRepository.findBySizeIdWithLock(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for size"));

        // Check available quantity (excluding reserved)
        int availableQuantity = inventory.getAvailableQuantity();
        if (availableQuantity < request.quantity()) {
            throw new InsufficientStockException("Insufficient stock. Available: " + availableQuantity + ", Requested: " + request.quantity());
        }

        // Generate reference number
        String referenceNumber = "EXPORT_" + request.sizeId() + "_" + System.currentTimeMillis();

        // Update stock quantity
        int oldQuantity = inventory.getQuantityInStock();
        inventory.setQuantityInStock(oldQuantity - request.quantity());

        // Save changes
        inventory = inventoryRepository.save(inventory);

        // Create transaction record
        StockTransaction transaction = StockTransaction.builder()
                .inventory(inventory)
                .transactionType(StockTransactionType.OUTBOUND)
                .quantity(request.quantity())
                .referenceNumber(referenceNumber)
                .notes(buildExportNotes(request))
                .createdBy(user)
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Exported {} units for size {} (old: {}, new: {})",
                request.quantity(), request.sizeId(), oldQuantity, inventory.getQuantityInStock());

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return inventoryMapper.mapToInventoryResponse(inventory, currentLanguage);
    }

    @Override
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        // Validate size exists
        sizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Size not found"));

        // Get current user
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Get inventory with lock
        Inventory inventory = inventoryRepository.findBySizeIdWithLock(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for size"));

        // Store old quantity for logging and notes
        int oldQuantity = inventory.getQuantityInStock();
        int quantityDifference = request.newQuantity() - oldQuantity;

        // Generate reference number
        String referenceNumber = "ADJUST_" + request.sizeId() + "_" + System.currentTimeMillis();

        // Update stock quantity
        inventory.setQuantityInStock(request.newQuantity());

        // Save changes
        inventory = inventoryRepository.save(inventory);

        // Determine transaction type based on adjustment
        StockTransactionType transactionType = quantityDifference > 0
                ? StockTransactionType.ADJUSTMENT_IN
                : StockTransactionType.ADJUSTMENT_OUT;

        // Create transaction record
        StockTransaction transaction = StockTransaction.builder()
                .inventory(inventory)
                .transactionType(transactionType)
                .quantity(Math.abs(quantityDifference))
                .referenceNumber(referenceNumber)
                .notes(buildAdjustmentNotes(request, oldQuantity, request.newQuantity()))
                .createdBy(user)
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Adjusted stock for size {} from {} to {} (difference: {})",
                request.sizeId(), oldQuantity, request.newQuantity(), quantityDifference);

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return inventoryMapper.mapToInventoryResponse(inventory, currentLanguage);
    }

    @Override
    public List<InventoryResponse> getInventoryReport() {
        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Fetch all inventories with product info
        List<Inventory> inventories = inventoryRepository.findAllWithProductInfo();

        return inventoryMapper.mapToInventoryResponseList(inventories, currentLanguage);
    }

    @Override
    public InventoryResponse getInventoryBySize(Long sizeId) {
        // Find inventory by size ID
        Inventory inventory = inventoryRepository.findBySizeId(sizeId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for size ID: " + sizeId));

        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return inventoryMapper.mapToInventoryResponse(inventory, currentLanguage);
    }

    @Override
    public InventoryStatsResponse getInventoryStats() {
        // Count total number of items in stock
        Long totalItems = inventoryRepository.count();

        // Total quantity of goods in stock
        Long totalStockQuantity = Optional.ofNullable(inventoryRepository.getTotalStockQuantity()).orElse(0L);

        // Total quantity of goods reserved
        Long totalReservedQuantity = Optional.ofNullable(inventoryRepository.getTotalReservedQuantity()).orElse(0L);

        // Total available quantity (in stock - reserved)
        Long totalAvailableQuantity = totalStockQuantity - totalReservedQuantity;

        // Count the number of items with inventory below the minimum level
        Long lowStockItemsCount = inventoryRepository.countLowStockItems();

        // Count the number of out of stock items (available quantity = 0)
        Long outOfStockItemsCount = inventoryRepository.countOutOfStockItems();

        // Calculate the percentage of low stock items
        Double lowStockPercentage = totalItems > 0 ? (lowStockItemsCount.doubleValue() / totalItems.doubleValue()) * 100 : 0.0;
        
        // Calculate the percentage of out of stock items
        Double outOfStockPercentage = totalItems > 0 ? (outOfStockItemsCount.doubleValue() / totalItems.doubleValue()) * 100 : 0.0;

        return new InventoryStatsResponse(
                totalItems,
                totalStockQuantity,
                totalReservedQuantity,
                totalAvailableQuantity,
                lowStockItemsCount,
                outOfStockItemsCount,
                Math.round(lowStockPercentage * 100.0) / 100.0,
                Math.round(outOfStockPercentage * 100.0) / 100.0
        );
    }

    @Override
    public List<InventoryResponse> getLowStockItems() {
        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Find items with inventory below the minimum level
        List<Inventory> lowStockInventories = inventoryRepository.findLowStockItems();

        log.info("Retrieved {} low stock items", lowStockInventories.size());
        return inventoryMapper.mapToInventoryResponseList(lowStockInventories, currentLanguage);
    }

    @Override
    public List<InventoryResponse> getOutOfStockItems() {
        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Find items with available quantity = 0
        List<Inventory> inventories = inventoryRepository.findAllWithProductInfo();

        // Filter out of stock items
        List<Inventory> outOfStockInventories = inventories.stream()
                .filter(inventory -> inventory.getAvailableQuantity() == 0)
                .toList();

        log.info("Retrieved {} out of stock items", outOfStockInventories.size());
        return inventoryMapper.mapToInventoryResponseList(outOfStockInventories, currentLanguage);
    }

    @Override
    public boolean checkAvailability(Long sizeId, int quantity) {
        // Find inventory by size ID
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeId(sizeId);

        // If inventory not found, no stock available
        if (inventoryOpt.isEmpty()) {
            log.warn("No inventory found for size ID: {}", sizeId);
            return false;
        }

        Inventory inventory = inventoryOpt.get();
        int availableQuantity = inventory.getAvailableQuantity();

        // Check if requested quantity is available
        boolean isAvailable = availableQuantity >= quantity;

        log.debug("Availability check for size {}: requested={}, available={}, result={}",
                sizeId, quantity, availableQuantity, isAvailable);

        return isAvailable;
    }

    @Override
    public void confirmReservedStock(Long sizeId, int quantity, String referenceNumber, User user, Order order) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeIdWithLock(sizeId);
        if (inventoryOpt.isEmpty()) {
            log.warn("Inventory not found for size ID: {}", sizeId);
            return;
        }

        Inventory inventory = inventoryOpt.get();

        // Trừ thật khỏi stock và giảm reserved
        if (inventory.getReservedQuantity() < quantity) {
            log.warn("Reserved quantity insufficient to confirm for sizeId={}, ref={}", sizeId, referenceNumber);
            quantity = inventory.getReservedQuantity(); // fallback
        }

        int oldStock = inventory.getQuantityInStock();
        inventory.setQuantityInStock(Math.max(0, inventory.getQuantityInStock() - quantity));
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));

        inventoryRepository.save(inventory);

        StockTransaction transaction = StockTransaction.builder()
                .inventory(inventory)
                .transactionType(StockTransactionType.OUTBOUND) // Trừ kho thực
                .quantity(quantity)
                .orderId(order != null ? order.getId() : null)
                .referenceNumber(referenceNumber)
                .notes("Confirmed stock for paid order")
                .createdBy(user)
                .build();

        stockTransactionRepository.save(transaction);

        log.info("Confirmed and deducted {} items for size ID: {}, ref={}, oldStock={}, newStock={}",
                quantity, sizeId, referenceNumber, oldStock, inventory.getQuantityInStock());
    }

    @Override
    public void releaseReservedStockByOrder(String orderNumber) {
        List<StockTransaction> reservedTransactions =
                stockTransactionRepository.findByReferenceNumberStartingWith("ORD-" + orderNumber);

        for (StockTransaction t : reservedTransactions) {
            releaseReservedStock(t.getInventory().getSize().getId(), t.getQuantity(), t.getReferenceNumber(), null);
        }

        log.info("Released reserved stock for order {}", orderNumber);
    }

    @Override
    public void processReturnStock(Order order) {
        log.info("Processing stock return for Order #{}", order.getOrderNumber());

        // Validate the order and its items
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            log.warn("Order or its items are null/empty. Cannot process stock return.");
            return;
        }

        // Get the user performing the action (likely an admin/staff)
        User currentUser = userRepository.findById(userService.getCurrentUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Iterate through each item in the returned order
        for (OrderItem item : order.getItems()) {
            if (item.getSize() == null) {
                log.warn("Skipping order item with ID {} because it has no size information.", item.getId());
                continue;
            }

            Long sizeId = item.getSize().getId();
            int returnedQuantity = item.getQuantity();

            // Find the inventory for the item's size with a pessimistic lock to prevent race conditions
            Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeIdWithLock(sizeId);

            if (inventoryOpt.isEmpty()) {
                // This is a critical issue, as a product that was sold must have had an inventory record.
                log.error("CRITICAL: Inventory not found for returned size ID: {}. Cannot process return for this item in order {}", sizeId, order.getOrderNumber());
                continue; // Skip this item and proceed with the next
            }

            Inventory inventory = inventoryOpt.get();
            int oldQuantity = inventory.getQuantityInStock();

            // Increase the stock quantity
            inventory.setQuantityInStock(oldQuantity + returnedQuantity);
            inventoryRepository.save(inventory);

            // Create a stock transaction log for auditing purposes
            StockTransaction transaction = StockTransaction.builder()
                    .inventory(inventory)
                    .transactionType(StockTransactionType.RETURN)
                    .quantity(returnedQuantity)
                    .orderId(order.getId())
                    .referenceNumber("RET-" + order.getOrderNumber())
                    .notes("Stock returned from order " + order.getOrderNumber())
                    .createdBy(currentUser)
                    .build();

            stockTransactionRepository.save(transaction);

            log.info("Returned {} units to stock for size ID {}. Old stock: {}, New stock: {}",
                    returnedQuantity, sizeId, oldQuantity, inventory.getQuantityInStock());
        }

        log.info("Finished processing stock return for Order #{}", order.getOrderNumber());
    }

    @Override
    public void restoreStockForConfirmedCancellation(Order order, User user) {
        if (order == null || order.getItems() == null) {
            log.warn("Order or items are null, cannot restore stock.");
            return;
        }

        log.info("Restoring stock for CONFIRMED cancellation of order {}", order.getOrderNumber());

        for (OrderItem item : order.getItems()) {

            // Get size ID and quantity from the order item
            Long sizeId = item.getSize().getId();
            int quantity = item.getQuantity();

            String referenceNumber = "CANCEL-" + order.getOrderNumber()
                    + "-PRV-" + item.getId().getProductVariantId()
                    + "-S-" + sizeId;

            // Find inventory with lock
            Optional<Inventory> invOpt = inventoryRepository.findBySizeIdWithLock(sizeId);

            if (invOpt.isEmpty()) {
                log.error("Inventory not found for size {} when restoring cancelled CONFIRMED order {}", sizeId, order.getOrderNumber());
                continue;
            }

            // Restore stock quantity
            Inventory inventory = invOpt.get();

            // Update stock quantity
            int oldQuantity = inventory.getQuantityInStock();
            inventory.setQuantityInStock(oldQuantity + quantity);
            inventoryRepository.save(inventory);

            // Create stock transaction
            StockTransaction txn = StockTransaction.builder()
                    .inventory(inventory)
                    .transactionType(StockTransactionType.RETURN)
                    .quantity(quantity)
                    .orderId(order.getId())
                    .referenceNumber(referenceNumber)
                    .notes("Stock returned from CONFIRMED -> CANCELED order")
                    .createdBy(user)
                    .build();

            stockTransactionRepository.save(txn);

            log.info("Restored {} units for size {} (oldStock={}, newStock={})",
                    quantity, sizeId, oldQuantity, inventory.getQuantityInStock());
        }
    }

    // Helper method to create a new inventory record for a size
    private Inventory createInventoryForSize(Size size) {
        return Inventory.builder()
                .size(size)
                .quantityInStock(0)
                .reservedQuantity(0)
                .minStockLevel(5)
                .build();
    }

    // Helper methods to build notes for transactions - Import
    private String buildImportNotes(ImportStockRequest request) {
        StringBuilder notes = new StringBuilder("Stock Import");
        if (request.supplierInfo() != null) {
            notes.append(" - Supplier: ").append(request.supplierInfo());
        }
        if (request.notes() != null) {
            notes.append(" - ").append(request.notes());
        }
        return notes.toString();
    }

    // Helper methods to build notes for transactions - Export
    private String buildExportNotes(ExportStockRequest request) {
        StringBuilder notes = new StringBuilder("Stock Export");
        if (request.reason() != null) {
            notes.append(" - Reason: ").append(request.reason());
        }
        if (request.notes() != null) {
            notes.append(" - ").append(request.notes());
        }
        return notes.toString();
    }

    // Helper methods to build notes for transactions - Adjustment
    private String buildAdjustmentNotes(StockAdjustmentRequest request, int oldQty, int newQty) {
        StringBuilder notes = new StringBuilder("Stock Adjustment");
        notes.append(" (").append(oldQty).append(" -> ").append(newQty).append(")");
        if (request.reason() != null) {
            notes.append(" - Reason: ").append(request.reason());
        }
        if (request.notes() != null) {
            notes.append(" - ").append(request.notes());
        }
        return notes.toString();
    }
}
