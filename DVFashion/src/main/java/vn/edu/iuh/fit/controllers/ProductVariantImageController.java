/*
 * @ {#} ProductVariantImageController.java   1.0     30/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;
import vn.edu.iuh.fit.services.ProductVariantImageService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description: Controller for managing product variant images
 * @author: Tran Hien Vinh
 * @date:   30/08/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/product-variants/{variantId}/images")
@RequiredArgsConstructor
public class ProductVariantImageController {
    private final ProductVariantImageService productVariantImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> addImageToVariant(
            @PathVariable("variantId") Long variantId,
            @Validated(ValidationGroups.Create.class) @RequestPart("imageInfo") ProductVariantImageRequest request,
            @RequestPart("imageFile") MultipartFile imageFile
    ) {
        ProductVariantImageResponse response = productVariantImageService.addImageToVariant(variantId, request, imageFile);
        return ResponseEntity.ok(ApiResponse.created(response, "Image added successfully"));
    }

    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateVariantImage(
            @PathVariable("imageId") Long imageId,
            @Validated(ValidationGroups.Update.class) @RequestPart(value = "imageInfo", required = false) ProductVariantImageRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductVariantImageResponse response = productVariantImageService.updateVariantImage(imageId, request, imageFile);
        return ResponseEntity.ok(ApiResponse.success(response, "Image updated successfully"));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<?>> getImageById(@PathVariable("imageId") Long imageId) {
        ProductVariantImageResponse response = productVariantImageService.getImageById(imageId);
        return ResponseEntity.ok(ApiResponse.success(response, "Image retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getImagesByVariantId(@PathVariable("variantId") Long variantId) {
        List<ProductVariantImageResponse> responses = productVariantImageService.getImagesByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Images retrieved successfully"));
    }
}
