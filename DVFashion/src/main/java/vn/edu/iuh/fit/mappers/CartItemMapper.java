/*
 * @ {#} CartItemMapper.java   1.0     10/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.CartItemResponse;
import vn.edu.iuh.fit.entities.CartItem;
import vn.edu.iuh.fit.entities.ProductTranslation;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.enums.Language;

import java.math.BigDecimal;

/*
 * @description: Mapper class for converting between CartItem entities and DTOs
 * @author: Tran Hien Vinh
 * @date:   10/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class CartItemMapper {
    public CartItemResponse mapToCartItemResponse(CartItem item, Language language) {
        // Get primary image
        String imageUrl = item.getProductVariant().getImages().stream()
                .filter(ProductVariantImage::isPrimary)
                .findFirst()
                .map(ProductVariantImage::getImageUrl)
                .orElse(null);

        // Get product name based on requested language
        String productName = getProductName(item, language);

        return new CartItemResponse(
                item.getId(),
                productName,
                item.getProductVariant().getColor(),
                item.getSize().getSizeName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                imageUrl,
                item.getReservedUntil()
        );
    }

    private String getProductName(CartItem item, Language language) {
        // Try to get name in requested language first
        String productName = item.getProductVariant()
                .getProduct()
                .getTranslations()
                .stream()
                .filter(translation -> translation.getLanguage() == language)
                .findFirst()
                .map(ProductTranslation::getName)
                .orElse(null);

        // If not found, fallback to Vietnamese
        if (productName == null) {
            productName = item.getProductVariant()
                    .getProduct()
                    .getTranslations()
                    .stream()
                    .filter(translation -> translation.getLanguage() == Language.VI)
                    .findFirst()
                    .map(ProductTranslation::getName)
                    .orElse("Unknown Product");
        }

        return productName;
    }
}
