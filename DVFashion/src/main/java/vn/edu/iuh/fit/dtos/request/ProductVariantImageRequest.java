/*
 * @ {#} ProductVariantImageRequest.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;

/*
 * @description: Request DTO for ProductVariantImage
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductVariantImageRequest(
        boolean isPrimary,

        @Min(value = 0, message = "Sort order must be >= 0")
        int sortOrder
) {}
