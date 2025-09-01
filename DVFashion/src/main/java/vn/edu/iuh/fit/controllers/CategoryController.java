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
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
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

    /**
     * API for creating a new category
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/categories
     *
     * 3. BODY (select form-data):
     *    - Key: "category"
     *      Type: Text
     *      Value: {"name":"Men's jeans"}
     *      Content-Type: application/json
     *
     *    - Key: "imageFile" (optional)
     *      Type: File
     *      Value: Select image file (.jpg, .png,...)
     *
     * 4. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 5. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "code": 201,
     *      "message": "Category created successfully.",
     *      "data": {
     *        "id": 1,
     *        "name": "Men's jeans",
     *        "description": "No description",
     *        "imageUrl": "http://domain.com/images/categories/phone-category.jpg",
     *        "active": true
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 409: Conflict - Category name already exists
     * - 400: Bad Request - Maximum upload size exceeded
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCategory(
            @Validated(ValidationGroups.Create.class) @RequestPart("category") CategoryRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.createCategory(request, imageFile, language);
        return ResponseEntity.ok(ApiResponse.created(categoryResponse, "Category created successfully."));
    }


    /**
     * API for updating an existing category
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: PUT
     * 2. URL: http://localhost:8080/api/v1/categories/{id}
     *
     * 3. BODY (select form-data):
     *    - Key: "category"
     *      Type: Text
     *      Value: {"name":"Updated Category Name", "description":"No description"}
     *      Content-Type: application/json
     *
     *    - Key: "imageFile" (optional)
     *      Type: File
     *      Value: Select image file (.jpg, .png,...)
     *
     * 4. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 5. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "message": "Category updated successfully.",
     *      "data": {
     *        "id": 1,
     *        "name": "Updated Category Name",
     *        "description": "No description",
     *        "imageUrl": "http://domain.com/images/categories/updated-image.jpg",
     *        "active": true
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 404: Not Found - Category with given ID does not exist
     * - 409: Conflict - Updated category name already exists
     * - 400: Bad Request - Maximum upload size exceeded
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateCategory(@PathVariable("id") Long id,
                                                         @Validated(ValidationGroups.Update.class) @RequestPart("category") CategoryRequest request,
                                                         @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                                         @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.updateCategory(request, id, imageFile, language);
        return ResponseEntity.ok(ApiResponse.success(categoryResponse, "Category updated successfully."));
    }


    /**
     * API for deactivating a category by ID
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: PATCH
     * 2. URL: http://localhost:8080/api/categories/{id}/deactivate
     *
     * 3. PATH PARAMETERS:
     *  - id: ID of the category to deactivate
     *
     * 4. SUCCESS RESPONSE (200):
     * {
     *   "success": true,
     *   "statusCode": 204,
     *   "message": "Category deactivated successfully.",
     * }
     *
     * COMMON ERRORS:
     * - 401 Unauthorized: If the user is not logged in or token is expired
     * - 400 Bad Request: If the action is not allowed for the current user role
     * - 404 Not Found: If the category with the given ID does not exist
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateCategory(@PathVariable("id") Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok(ApiResponse.noContent("Category deactivated successfully."));
    }


    /**
     * API for retrieving categories with pagination
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/categories
     *
     * 3. QUERY PARAMETERS:
     *  - page: Page number (default is 0)
     *  - size: Number of items per page (default is 12)
     *  - sort: Field to sort by (default is "id")
     *  - direction: Sort direction, either ASC or DESC (default is ASC)
     *  - lang: Language for category data, either VI or EN (default is VI)
     *
     * 4. SUCCESS RESPONSE (200):
     * {
     *     "success": true,
     *     "statusCode": 200,
     *     "message": "Categories retrieved successfully.",
     *     "data": {
     *         "page": 0,
     *         "size": 12,
     *         "totalElements": 13,
     *         "totalPages": 2,
     *         "sorts": [
     *             "id: ASC"
     *         ],
     *         "values": [
     *             {
     *                 "id": 18,
     *                 "name": "Máy tính bảng Ipad",
     *                 "description": "Không có mô tả",
     *                 "imageUrl": "https://res.cloudinary.com/yowiehw5qfuzc94ecot6.jpg",
     *                 "active": false
     *             },
     *             {
     *                 "id": 19,
     *                 "name": "Giày nam",
     *                 "description": "Không có mô tả.",
     *                 "imageUrl": "https://res.cloudinary.com/diilgkg1a/image/upload/v1755791598/no-image.png",
     *                 "active": true
     *             }
     *         ]
     *     }
     * }
     *
     * COMMON ERRORS:
     * - 401 Unauthorized: If the user is not logged in or token is expired
     * - 400 Bad Request: If the action is not allowed for the current user role
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCategoriesPaging(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language
    ) {
        PageResponse<CategoryResponse> categories = categoryService.getCategoriesPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully."));
    }


    /**
    * API for retrieving a category by ID
    *
    * HOW TO TEST WITH POSTMAN:
    * 1. METHOD: GET
    * 2. URL: http://localhost:8080/api/categories/{id}
    *
    * 3. PATH PARAMETERS:
    * - id: ID of the category to retrieve
    *
    * 4. QUERY PARAMETERS:
    * - lang: Language for category data, either VI or EN (default is VI)
    *
    * 5. SUCCESS RESPONSE (200):
    * {
    *   "success": true,
    *   "statusCode": 201,
    *   "message": "Category retrieved successfully.",
    *   "data": {
    *      "id": 1,
    *      "name": "Women's jeans",
    *      "description": "No description.",
    *      "imageUrl": "https://res.cloudinary.com/diilgkg1a/image/upload/v1755791598/no-image.png",
    *      "active": true
    *   }
    * }
    * COMMON ERRORS:
    * - 401 Unauthorized: If the user is not logged in or token is expired
    * - 400 Bad Request: If the action is not allowed for the current user role
    * - 404 Not Found: If the category with the given ID does not exist
    *
    */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCategoryById(@PathVariable("id") Long id,
                                                          @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id, language);
        return ResponseEntity.ok(ApiResponse.success(categoryResponse, "Category retrieved successfully."));
    }

    /**
     * API for retrieving all categories without pagination
     *
     * HOW TO TEST WITH POSTMAN:
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/categories/all
     *
     * 3. QUERY PARAMETERS:
     * - lang: Language for category data, either VI or EN (default is VI)
     *
     * 4. SUCCESS RESPONSE (200):
     * {
     *   "success": true,
     *   "statusCode": 200,
     *   "message": "Categories retrieved successfully.",
     *   "data": [
     *      {
     *        "id": 1,
     *        "name": "Quần jean nam",
     *        "description": "Không có mô tả",
     *        "imageUrl": "https://res.cloudinary.com/diilgkg1a/image/upload/v1755791598/no-image.png",
     *        "active": true
     *       }
     *    ]
     * }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllCategoriesNoPaging(@RequestParam(value = "lang", defaultValue = "VI") Language language) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(language),
                "Categories retrieved successfully."));
    }
}
