/*
 * @ {#} SizeService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.entities.Size;

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
     * @return The created Size entity.
     */
    Size createSize(Long variantId, SizeRequest request);
}
