/*
 * @ {#} ProductController.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/products")
public class ProductController {
    private final ProductService productService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Validated(ValidationGroups.Create.class)
            @RequestPart("product") ProductRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages) {

        ProductResponse response = productService.createProduct(request, language, variantImages);
        return ResponseEntity.ok(ApiResponse.created(response, "Product created successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("productId") Long productId,
            @Validated(ValidationGroups.Update.class)
            @RequestBody ProductRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        ProductResponse response = productService.updateProduct(productId, request, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        ProductResponse response = productService.getProductById(productId, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProductsNoPaging(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        List<ProductResponse> products = productService.getAllProducts(language);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<ProductResponse> products = productService.getProductsPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Brands retrieved successfully."));
    }
}
