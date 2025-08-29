/*
 * @ {#} ProductVariantImageMapper.java   1.0     30/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;
import vn.edu.iuh.fit.entities.ProductVariantImage;

/*
 * @description: Mapper for ProductVariantImage entity to ProductVariantImageResponse DTO
 * @author: Tran Hien Vinh
 * @date:   30/08/2025
 * @version:    1.0
 */
@Component
public class ProductVariantImageMapper {
    public ProductVariantImageResponse toResponse(ProductVariantImage image) {
        if (image == null) {
            return null;
        }

        return new ProductVariantImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.isPrimary(),
                image.getSortOrder(),
                image.getProductVariant() != null ? image.getProductVariant().getId() : null
        );
    }
}
