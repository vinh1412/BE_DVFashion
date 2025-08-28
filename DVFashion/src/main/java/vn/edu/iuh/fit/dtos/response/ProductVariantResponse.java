/*
 * @ {#} ProductVariantResponse.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for ProductVariant response
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductVariantResponse(
        Long id,

        String color,

        BigDecimal additionalPrice,

        String status,

        List<SizeResponse> sizes,

        List<ProductVariantImageResponse> images
) {}
