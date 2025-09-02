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

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCategory(
            @Validated(ValidationGroups.Create.class) @RequestPart("brand") BrandRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.createBrand(request, logoFile, language);
        return ResponseEntity.ok(ApiResponse.created(brandResponse, "Brand created successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBrandById(
            @PathVariable("id") Long id,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.getBrandById(id, language);
        return ResponseEntity.ok(ApiResponse.created(brandResponse, "Brand retrieved successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateBrand(@PathVariable("id") Long id,
                                                         @Validated(ValidationGroups.Update.class) @RequestPart("brand") BrandRequest request,
                                                         @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
                                                         @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        BrandResponse brandResponse = brandService.updateBrand(request, id, logoFile, language);
        return ResponseEntity.ok(ApiResponse.success(brandResponse, "Brand updated successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateBrand(@PathVariable("id") Long id) {
        brandService.deactivateBrand(id);
        return ResponseEntity.ok(ApiResponse.noContent("Brand deactivated successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands(
            @PageableDefault(page = 0, size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PageResponse<BrandResponse> brands = brandService.getBrandsPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(brands, "Brands retrieved successfully."));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllBrandsNoPaging(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrands(language), "Brands retrieved successfully."));
    }
}
