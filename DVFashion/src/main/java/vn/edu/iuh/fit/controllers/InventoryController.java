/*
 * @ {#} InventoryController.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.ExportStockRequest;
import vn.edu.iuh.fit.dtos.request.ImportStockRequest;
import vn.edu.iuh.fit.dtos.request.StockAdjustmentRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.InventoryResponse;
import vn.edu.iuh.fit.dtos.response.InventoryStatsResponse;
import vn.edu.iuh.fit.services.InventoryService;

import java.util.List;

/*
 * @description: Controller for inventory management
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/inventories")
public class InventoryController {
    private final InventoryService inventoryService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<?>> importStock(@Valid @RequestBody ImportStockRequest request) {
        InventoryResponse response = inventoryService.importStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock imported successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<?>> exportStock(
            @Valid @RequestBody ExportStockRequest request) {

        InventoryResponse response = inventoryService.exportStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock exported successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<?>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {

        InventoryResponse response = inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<?>>> getInventoryReport() {
        List<InventoryResponse> response = inventoryService.getInventoryReport();
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory report retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/size/{sizeId}")
    public ResponseEntity<ApiResponse<?>> getInventoryBySizeId(@PathVariable Long sizeId) {
        InventoryResponse response = inventoryService.getInventoryBySize(sizeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getInventoryStats() {
        InventoryStatsResponse response = inventoryService.getInventoryStats();
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory statistics retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<?>>> getLowStockItems() {
        List<InventoryResponse> response = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock items retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<?>>> getOutOfStockItems() {
        List<InventoryResponse> response = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.success(response, "Out of stock items retrieved successfully."));
    }

}
