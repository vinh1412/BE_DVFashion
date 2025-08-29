/*
 * @ {#} SizeService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.dtos.response.SizeResponse;

import java.util.List;

/*
 * @description: Service interface for managing Sizes
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public interface SizeService {
    /**
     * Creates a new size for the specified product variant with the given request data.
     *
     * @param variantId The ID of the product variant to which the size belongs.
     * @param request   The size request data.
     * @return The created SizeResponse.
     */
    SizeResponse createSize(Long variantId, SizeRequest request);

    /**
     * Updates an existing size with the specified ID using the provided request data.
     *
     * @param sizeId  The ID of the size to be updated.
     * @param request The size request data.
     * @return The updated SizeResponse.
     */
    SizeResponse updateSize(Long sizeId, SizeRequest request);

    /**
     *  Gets the size details by its ID.
     * @param sizeId The ID of the size to retrieve.
     * @return The SizeResponse containing size details.
     */
    SizeResponse getSizeById(Long sizeId);

    /**
     * Retrieves a list of sizes associated with the specified product variant ID.
     *
     * @param variantId The ID of the product variant.
     * @return A list of SizeResponse objects associated with the product variant.
     */
    List<SizeResponse> getSizesByVariantId(Long variantId);
}
