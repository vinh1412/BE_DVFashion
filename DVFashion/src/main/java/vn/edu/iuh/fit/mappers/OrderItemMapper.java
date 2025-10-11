/*
 * @ {#} OrderItemMapper.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.OrderItemResponse;
import vn.edu.iuh.fit.entities.OrderItem;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.ProductTranslation;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.enums.Language;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Mapper class for converting OrderItem entities to OrderItemResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Component
public class OrderItemMapper {
    public List<OrderItemResponse> mapOrderItemResponses(List<OrderItem> items, Language language) {
        return items.stream().map(item -> {
            String imageUrl = item.getProductVariant().getImages().stream()
                    .filter(ProductVariantImage::isPrimary)
                    .findFirst()
                    .map(ProductVariantImage::getImageUrl)
                    .orElse("");

            return new OrderItemResponse(
                    item.getProductVariant().getId(),
                    getProductName(item.getProductVariant().getProduct(), language),
                    item.getProductVariant().getColor(),
                    item.getSize().getSizeName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscount(),
                    item.getUnitPrice().subtract(item.getDiscount()).multiply(BigDecimal.valueOf(item.getQuantity())),
                    imageUrl
            );
        }).toList();
    }

    private String getProductName(Product product,Language language) {
        return product.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .map(ProductTranslation::getName)
                .orElse("Unknown Product");
    }
}
