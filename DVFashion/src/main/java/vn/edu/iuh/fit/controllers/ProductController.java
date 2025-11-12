/*
 * @ {#} ProductController.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import vn.edu.iuh.fit.dtos.response.ProductStatisticsResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: Controller for managing products
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

//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProductsNoPaging(
//            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
//
//        List<ProductResponse> products = productService.getAllProducts(language);
//        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<ProductResponse> products = productService.getProductsPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Brands retrieved successfully."));
    }

    @GetMapping("/promotion/{promotionId}")
    public ResponseEntity<ApiResponse<?>> getProductsByPromotionId(
            @PathVariable Long promotionId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        List<ProductResponse> products = productService.getProductsByPromotionId(promotionId, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Products from promotion retrieved successfully."));
    }

    @GetMapping("/promotion/{promotionId}/paging")
    public ResponseEntity<ApiResponse<?>> getProductsByPromotionIdPaging(
            @PathVariable Long promotionId,
            @PageableDefault(page = 0, size = 12) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<ProductResponse> products = productService.getProductsByPromotionIdPaging(promotionId, pageable, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Products from promotion retrieved successfully with pagination."));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<?>> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        List<ProductResponse> products = productService.getProductsByCategoryId(categoryId, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Products from category retrieved successfully."));
    }

    @GetMapping("/category/{categoryId}/paging")
    public ResponseEntity<ApiResponse<?>> getProductsByCategoryIdPaging(
            @PathVariable Long categoryId,
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<ProductResponse> products = productService.getProductsByCategoryIdPaging(categoryId, pageable, language);
        return ResponseEntity.ok(ApiResponse.success(products, "Products from category retrieved successfully with pagination."));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be less than zero")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must not be less than one")
            int size,

            @RequestParam(name = "sort", required = false)
            String[] sort,

            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "categoryId", required = false)
            Long categoryId,

            @RequestParam(name = "promotionId", required = false)
            Long promotionId,

            @RequestParam(name = "status", required = false)
            ProductStatus status,

            @RequestParam(name = "onSale", required = false)
            Boolean onSale,

            @RequestParam(name = "minPrice", required = false)
            BigDecimal minPrice,

            @RequestParam(name = "maxPrice", required = false)
            BigDecimal maxPrice,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd
            LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd
            LocalDate endDate,

            @RequestParam(name = "language", defaultValue = "VI")
            Language language
    ) {
        PageResponse<ProductResponse> response = productService.getAllProducts(
                page, size, sort, search, categoryId, promotionId, status,
                onSale, minPrice, maxPrice, startDate, endDate, language
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Fetched products successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ProductStatisticsResponse>> getProductStatistics() {
        ProductStatisticsResponse productStatistics = productService.getProductStatistics();
        return ResponseEntity.ok(ApiResponse.success(productStatistics, "Product statistics fetched successfully"));
    }
}
