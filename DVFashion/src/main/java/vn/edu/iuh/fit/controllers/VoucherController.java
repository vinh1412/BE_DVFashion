/*
 * @ {#} VoucherController.java   1.0     02/11/2025
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
import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
import vn.edu.iuh.fit.dtos.request.UpdateVoucherRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.VoucherService;

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
}
