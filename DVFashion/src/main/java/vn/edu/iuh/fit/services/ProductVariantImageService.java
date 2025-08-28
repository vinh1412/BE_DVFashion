/*
 * @ {#} ProductVariantImageService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.entities.ProductVariantImage;

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
    ProductVariantImage addImageToVariant(Long variantId, ProductVariantImageRequest request, MultipartFile imageFile);
}
