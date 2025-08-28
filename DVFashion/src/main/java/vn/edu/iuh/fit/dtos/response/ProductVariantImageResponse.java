/*
 * @ {#} ProductVariantImageResponse.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for ProductVariantImage response
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductVariantImageResponse(
        Long id,

        String imageUrl,

        boolean isPrimary,

        int sortOrder
) {}
