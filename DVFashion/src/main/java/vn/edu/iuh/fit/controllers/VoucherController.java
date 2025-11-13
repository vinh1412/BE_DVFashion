/*
 * @ {#} VoucherController.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
import vn.edu.iuh.fit.dtos.request.UpdateVoucherRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.dtos.response.VoucherStatisticsResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.VoucherService;

import java.util.List;

/*
 * @description: REST controller for Voucher management
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        VoucherResponse response = voucherService.createVoucher(request, language);
        return ResponseEntity.ok(ApiResponse.created(response, "Voucher created successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateVoucherRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        VoucherResponse response = voucherService.updateVoucher(id, request, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Voucher updated successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @DeleteMapping("/{voucherId}/products/{productId}")
    public ResponseEntity<ApiResponse<VoucherResponse>> removeProductFromVoucher(
            @PathVariable(name = "voucherId") Long voucherId,
            @PathVariable(name = "productId") Long productId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        VoucherResponse response = voucherService.removeProductFromVoucher(voucherId, productId, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Product removed from voucher successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @DeleteMapping("/{voucherId}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @PathVariable(name = "voucherId") Long voucherId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        voucherService.deleteVoucher(voucherId, language);
        return ResponseEntity.ok(ApiResponse.success(null, "Voucher deleted successfully"));
    }

    @GetMapping("/{voucherId}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(
            @PathVariable(name = "voucherId") Long voucherId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        VoucherResponse response = voucherService.getVoucherById(voucherId, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Voucher retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllVouchersForAdmin(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        List<VoucherResponse> response = voucherService.getAllVouchersForAdmin(language);
        return ResponseEntity.ok(ApiResponse.success(response, "All vouchers retrieved successfully"));
    }

    @GetMapping("/customer/all")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllAvailableVouchersForCustomer(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        List<VoucherResponse> response = voucherService.getAllAvailableVouchersForCustomer(language);
        return ResponseEntity.ok(ApiResponse.success(response, "All available vouchers retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> getVouchersForAdminPaging(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        PageResponse<VoucherResponse> response = voucherService.getVouchersForAdminPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Vouchers retrieved successfully with pagination"));
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> getAvailableVouchersForCustomerPaging(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        PageResponse<VoucherResponse> response = voucherService.getAvailableVouchersForCustomerPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Available vouchers retrieved successfully with pagination"));
    }

    @GetMapping("/statistics")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<VoucherStatisticsResponse>> getVoucherStatistics() {
        VoucherStatisticsResponse statistics = voucherService.getVoucherStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Voucher statistics retrieved successfully"));
    }
}
