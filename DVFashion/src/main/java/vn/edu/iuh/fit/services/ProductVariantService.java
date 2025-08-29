/*
 * @ {#} ProductVariantService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.response.ProductVariantResponse;

import java.util.List;

/*
 * @description: Service interface for managing ProductVariants
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public interface ProductVariantService {
    /**
     * Creates a new product variant for the specified product with the given request data and variant images.
     *
     * @param productId     The ID of the product to which the variant belongs.
     * @param request       The product variant request data.
     * @param variantImages A list of images for the product variant.
     * @return The created product variant response.
     */
    ProductVariantResponse createProductVariant(Long productId, ProductVariantRequest request, List<MultipartFile> variantImages);

    ProductVariantResponse updateProductVariant(Long variantId, ProductVariantRequest request);

    ProductVariantResponse getProductVariantById(Long variantId);
}
