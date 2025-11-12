/*
 * @ {#} CategoryController.java   1.0     21/08/2025
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
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.CategoryService;
import vn.edu.iuh.fit.validators.ValidationGroups;

/*
 * @description: Controller for managing categories
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCategory(
            @Validated(ValidationGroups.Create.class) @RequestPart("category") CategoryRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.createCategory(request, imageFile, language);
        return ResponseEntity.ok(ApiResponse.created(categoryResponse, "Category created successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateCategory(@PathVariable("id") Long id,
                                                         @Validated(ValidationGroups.Update.class) @RequestPart("category") CategoryRequest request,
                                                         @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                                         @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.updateCategory(request, id, imageFile, language);
        return ResponseEntity.ok(ApiResponse.success(categoryResponse, "Category updated successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateCategory(@PathVariable("id") Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok(ApiResponse.noContent("Category deactivated successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCategoriesPaging(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language
    ) {
        PageResponse<CategoryResponse> categories = categoryService.getCategoriesPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCategoryById(@PathVariable("id") Long id,
                                                          @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id, language);
        return ResponseEntity.ok(ApiResponse.success(categoryResponse, "Category retrieved successfully."));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllCategoriesNoPaging(@RequestParam(value = "lang", defaultValue = "VI") Language language) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(language),
                "Categories retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<CategoryStatisticsResponse>> getCategoryStatistics() {
        CategoryStatisticsResponse productStatistics = categoryService.getCategoryStatistics();
        return ResponseEntity.ok(ApiResponse.success(productStatistics, "Product statistics fetched successfully"));
    }
}
