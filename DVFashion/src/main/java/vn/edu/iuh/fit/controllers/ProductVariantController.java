/*
 * @ {#} ProductVariantController.java   1.0     29/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ProductVariantResponse;
import vn.edu.iuh.fit.services.ProductVariantService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description: Controller for managing Product Variants
 * @author: Tran Hien Vinh
 * @date:   29/08/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService productVariantService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> addProductVariant(
            @PathVariable("productId") Long productId,
            @Validated(ValidationGroups.Create.class) @RequestPart("variant") ProductVariantRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> variantImages
    ) {
        ProductVariantResponse response = productVariantService.createProductVariant(productId, request, variantImages);
        return ResponseEntity.ok(ApiResponse.created(response, "Product variant added successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{variantId}")
    public ResponseEntity<ApiResponse<?>> updateProductVariant(
            @PathVariable("variantId") Long variantId,
            @Validated(ValidationGroups.Update.class) @RequestBody ProductVariantRequest request
    ) {
        ProductVariantResponse response = productVariantService.updateProductVariant(variantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product variant updated successfully"));
    }

    @GetMapping(value = "/{variantId}")
    public ResponseEntity<ApiResponse<?>> getProductVariantById(@PathVariable("variantId") Long variantId) {
        ProductVariantResponse response = productVariantService.getProductVariantById(variantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Product variant retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProductVariantsByProductId(@PathVariable("productId") Long productId) {
        List<ProductVariantResponse> response = productVariantService.getProductVariantsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Product variants retrieved successfully"));
    }
}
