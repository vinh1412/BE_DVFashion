/*
 * @ {#} ProductResponse.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO for Product response
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductResponse(
        Long id,

        String name,

        String description,

        BigDecimal price,

        String material,

        BigDecimal salePrice,

        boolean onSale,

        String status,

        String categoryName,

        String brandName,

        String promotionName,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<ProductVariantResponse> variants
) {}
