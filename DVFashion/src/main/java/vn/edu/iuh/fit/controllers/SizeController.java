/*
 * @ {#} SizeController.java   1.0     30/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.SizeResponse;
import vn.edu.iuh.fit.services.SizeService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description: Controller for managing product variant images
 * @author: Tran Hien Vinh
 * @date:   30/08/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/product-variants/{variantId}/sizes")
@RequiredArgsConstructor
public class SizeController {
    private final SizeService sizeService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addSize(
            @PathVariable("variantId") Long variantId,
            @Validated(ValidationGroups.Create.class) @RequestBody SizeRequest request
    ) {
        SizeResponse size = sizeService.createSize(variantId, request);
        return ResponseEntity.ok(ApiResponse.created(size, "Size added successfully"));
    }

    @PutMapping(value = "/{sizeId}")
    public ResponseEntity<ApiResponse<?>> updateSize(
            @PathVariable("sizeId") Long sizeId,
            @Validated(ValidationGroups.Update.class) @RequestBody SizeRequest request
    ) {
        SizeResponse size = sizeService.updateSize(sizeId, request);
        return ResponseEntity.ok(ApiResponse.success(size, "Size updated successfully"));
    }

    @GetMapping(value = "/{sizeId}")
    public ResponseEntity<ApiResponse<?>> getSizeById(@PathVariable("sizeId") Long sizeId) {
        SizeResponse size = sizeService.getSizeById(sizeId);
        return ResponseEntity.ok(ApiResponse.success(size, "Size retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllSizesByVariantId(@PathVariable("variantId") Long variantId) {
        List<SizeResponse> sizes = sizeService.getSizesByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success(sizes, "Sizes retrieved successfully"));
    }
}
