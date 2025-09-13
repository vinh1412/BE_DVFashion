/*
 * @ {#} ProductVariantMapper.java   1.0     29/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

/*
 * @description: Mapper class for converting ProductVariant entities to ProductVariantResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   29/08/2025
 * @version:    1.0
 */

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;
import vn.edu.iuh.fit.dtos.response.ProductVariantResponse;
import vn.edu.iuh.fit.dtos.response.SizeResponse;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.entities.Size;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductVariantMapper {

    public ProductVariantResponse toResponse(ProductVariant variant) {
        List<SizeResponse> sizeResponses = new ArrayList<>();
        for (Size size : variant.getSizes()) {
            SizeResponse sizeResponse = new SizeResponse(
                    size.getId(), size.getSizeName(),
                    size.getStockQuantity(),
                    size.getAvailableQuantity(),
                    size.getProductVariant().getId());
            sizeResponses.add(sizeResponse);
        }

        List<ProductVariantImageResponse> productVariantImageResponse = new ArrayList<>();
        for (ProductVariantImage image : variant.getImages()) {
            ProductVariantImageResponse imageResponse = new ProductVariantImageResponse(image.getId(),
                    image.getImageUrl(), image.isPrimary(), image.getSortOrder(), image.getProductVariant().getId());
            productVariantImageResponse.add(imageResponse);
        }

        return new ProductVariantResponse(
                variant.getId(),
                variant.getColor(),
                variant.getAddtionalPrice(),
                variant.getStatus().name(),
                sizeResponses,
                productVariantImageResponse
        );
    }
}
