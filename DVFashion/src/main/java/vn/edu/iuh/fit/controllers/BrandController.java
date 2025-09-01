/*
 * @ {#} BrandController.java   1.0     27/08/2025
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
import vn.edu.iuh.fit.dtos.request.BrandRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.BrandResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.BrandService;
import vn.edu.iuh.fit.validators.ValidationGroups;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/brands")
public class BrandController {
    private final BrandService brandService;

    /**
     * API for creating a new brand
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/brands
     *
     * 3. BODY (select form-data):
     *    - Key: "brand"
     *      Type: Text
     *      Value: {"name":"Gucci","description":"Luxury brand"}
     *      Content-Type: application/json
     *
     *    - Key: "logoFile" (optional)
     *      Type: File
     *      Value: Select image file (.jpg, .png,...)
     *
     * 4. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 5. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 201,
     *      "message": "Brand created successfully.",
     *      "data": {
     *        "id": 1,
     *        "name": "Gucci",
     *        "description": "Thương hiệu cao cấp",
     *        "logo": "http://domain.com/images/brand/abc.jpg",
     *        "active": true
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 409: Conflict - Brand name already exists
     * - 400: Bad Request - Maximum upload size exceeded
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCategory(
            @Validated(ValidationGroups.Create.class) @RequestPart("brand") BrandRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.createBrand(request, logoFile, language);
        return ResponseEntity.ok(ApiResponse.created(brandResponse, "Brand created successfully."));
    }


    /**
     * API for retrieving a brand by ID
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/v1/brands/{id}
     *    - Replace {id} with the actual brand ID to retrieve
     *
     * 3. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 4. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 201,
     *      "message": "Brand retrieved successfully.",
     *      "data": {
     *        "id": 1,
     *        "name": "Gucci",
     *        "description": "Thương hiệu cao cấp",
     *        "logo": "http://domain.com/images/brand/abc.jpg",
     *        "active": true
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 404: Not Found - Brand with specified ID does not exist
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBrandById(
            @PathVariable("id") Long id,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.getBrandById(id, language);
        return ResponseEntity.ok(ApiResponse.created(brandResponse, "Brand retrieved successfully."));
    }


    /**
     * API for updating an existing brand
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: PUT
     * 2. URL: http://localhost:8080/api/v1/brands/{id}
     *    - Replace {id} with the actual brand ID to update
     *
     * 3. BODY (select form-data):
     *    - Key: "brand"
     *      Type: Text
     *      Value: {"name":"Updated Brand Name","description":"Updated description"}
     *      Content-Type: application/json
     *
     *    - Key: "logoFile" (optional)
     *      Type: File
     *      Value: Select new image file (.jpg, .png,...) if updating logo
     *
     * 4. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 5. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 200,
     *      "message": "Brand updated successfully.",
     *      "data": {
     *        "id": 1,
     *        "name": "Updated Brand Name",
     *        "description": "Cập nhật mô tả",
     *        "logo": "http://domain.com/images/brand/updated.jpg",
     *        "active": true
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 404: Not Found - Brand with specified ID does not exist
     * - 409: Conflict - Updated brand name already exists
     * - 400: Bad Request - Maximum upload size exceeded
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateBrand(@PathVariable("id") Long id,
                                                         @Validated(ValidationGroups.Update.class) @RequestPart("brand") BrandRequest request,
                                                         @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
                                                         @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.updateBrand(request, id, logoFile, language);
        return ResponseEntity.ok(ApiResponse.success(brandResponse, "Brand updated successfully."));
    }


    /**
     * API for deactivating a brand by ID
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: PATCH
     * 2. URL: http://localhost:8080/api/v1/brands/{id}/deactivate
     *    - Replace {id} with the actual brand ID to deactivate
     *
     * 3. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 204,
     *      "message": "Brand deactivated successfully."
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     * - 404: Not Found - Brand with specified ID does not exist
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateBrand(@PathVariable("id") Long id) {
        brandService.deactivateBrand(id);
        return ResponseEntity.ok(ApiResponse.noContent("Brand deactivated successfully."));
    }


    /**
     * API for retrieving all brands with pagination
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/v1/brands
     *
     * 3. QUERY PARAMETERS:
     *    - page: Page number (default is 0)
     *    - size: Number of items per page (default is 12)
     *    - sort: Field to sort by (default is "id")
     *    - direction: Sort direction, either ASC or DESC (default is ASC)
     *    - lang: VI (default) or EN
     *
     * 4. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 200,
     *      "message": "Brands retrieved successfully.",
     *      "data": {
     *        "values": [
     *          {
     *            "id": 1,
     *            "name": "Gucci",
     *            "description": "Thương hiệu cao cấp",
     *            "logo": "http://domain.com/images/brand/abc.jpg",
     *            "active": true
     *          },
     *          ...
     *        ],
     *        "page": 0,
     *        "size": 12,
     *        "totalElements": 1,
     *        "totalPages": 1,
     *        "sorts": [
     *           "id: ASC"
     *        ]
     *      }
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     */
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<BrandResponse> brands = brandService.getBrandsPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(brands, "Brands retrieved successfully."));
    }

    /**
     * API for retrieving all brands without pagination
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/v1/brands/all
     *
     * 3. QUERY PARAMETERS:
     *    - lang: VI (default) or EN
     *
     * 4. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "status": 200,
     *      "message": "Brands retrieved successfully.",
     *      "data": [
     *        {
     *          "id": 1,
     *          "name": "Gucci",
     *          "description": "Thương hiệu cao cấp",
     *          "logo": "http://domain.com/images/brand/abc.jpg",
     *          "active": true
     *        },
     *        ...
     *      ]
     *    }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - Not logged in or token expired
     * - 400: Bad Request - Action not allowed for current user role
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllBrandsNoPaging(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrands(language), "Brands retrieved successfully."));
    }
}
