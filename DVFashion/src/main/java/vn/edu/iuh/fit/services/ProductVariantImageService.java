/*
 * @ {#} ProductVariantImageService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;

import java.util.List;

/*
 * @description: Service interface for managing ProductVariantImage
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public interface ProductVariantImageService {
    /**
     * Adds an image to a product variant.
     *
     * @param variantId The ID of the product variant.
     * @param request   The request data for the product variant image.
     * @param imageFile The image file to be added.
     * @return The created ProductVariantImage entity.
     */
    ProductVariantImageResponse addImageToVariant(Long variantId, ProductVariantImageRequest request, MultipartFile imageFile);

    /**
     * Updates an existing product variant image.
     *
     * @param imageId   The ID of the product variant image to be updated.
     * @param request   The request data for updating the product variant image.
     * @param imageFile The new image file (optional).
     * @return The updated ProductVariantImage entity.
     */
    ProductVariantImageResponse updateVariantImage(Long imageId, ProductVariantImageRequest request, MultipartFile imageFile);

    /**
     *  Gets a product variant image by its ID.
     * @param imageId The ID of the product variant image.
     * @return The ProductVariantImageResponse DTO.
     */
    ProductVariantImageResponse getImageById(Long imageId);

    /**
     * Gets all images associated with a specific product variant.
     *
     * @param variantId The ID of the product variant.
     * @return A list of ProductVariantImageResponse DTOs.
     */
    List<ProductVariantImageResponse> getImagesByVariantId(Long variantId);
}
