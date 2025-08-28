/*
 * @ {#} ProductMapper.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;

/*
 * @description: Mapper class for converting Product entities to ProductResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ProductVariantMapper productVariantMapper;

    public ProductResponse toResponse(Product product, ProductTranslation translation, String categoryName, String brandName) {

        return new ProductResponse(
                product.getId(),
                translation.getName(),
                translation.getDescription(),
                product.getPrice(),
                translation.getMaterial(),
                product.getSalePrice(),
                product.isOnSale(),
                product.getStatus().name(),
                categoryName,
                brandName,
                product.getPromotion() != null ? product.getPromotion().getName() : null,
                product.getVariants().stream()
                        .map(productVariantMapper::toResponse)
                        .toList()
        );
    }
}
