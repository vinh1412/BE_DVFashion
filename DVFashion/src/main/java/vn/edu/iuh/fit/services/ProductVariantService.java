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

    /**
     * Updates an existing product variant identified by the given variant ID with the provided request data.
     *
     * @param variantId The ID of the product variant to be updated.
     * @param request   The product variant request data for the update.
     * @return The updated product variant response.
     */
    ProductVariantResponse updateProductVariant(Long variantId, ProductVariantRequest request);

    /**
     * Retrieves the product variant identified by the specified variant ID.
     *
     * @param variantId The ID of the product variant to be retrieved.
     * @return The product variant response.
     */
    ProductVariantResponse getProductVariantById(Long variantId);

    /**
     * Retrieves a list of product variants associated with the specified product ID.
     *
     * @param productId The ID of the product whose variants are to be retrieved.
     * @return A list of product variant responses.
     */
    List<ProductVariantResponse> getProductVariantsByProductId(Long productId);
}
