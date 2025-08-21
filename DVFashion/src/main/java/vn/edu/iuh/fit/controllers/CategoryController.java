/*
 * @ {#} CategoryController.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.services.CategoryService;

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
    public ResponseEntity<ApiResponse<?>> createCategory(@RequestPart("category") CategoryRequest request,
                                                         @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        CategoryResponse categoryResponse = categoryService.createCategory(request, imageFile);
        return ResponseEntity.ok(ApiResponse.created(categoryResponse, "Category created successfully."));
    }
}
