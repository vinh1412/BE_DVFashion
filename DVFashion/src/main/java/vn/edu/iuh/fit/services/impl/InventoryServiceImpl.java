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
import vn.edu.iuh.fit.entities.Inventory;
import vn.edu.iuh.fit.entities.Size;
import vn.edu.iuh.fit.entities.StockTransaction;
import vn.edu.iuh.fit.entities.User;
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
