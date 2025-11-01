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
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.repositories.PromotionProductRepository;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.math.BigDecimal;

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

    private final PromotionProductRepository promotionProductRepository;

    public ProductResponse toResponse(Product product, ProductTranslation translation, String categoryName) {

        return new ProductResponse(
                product.getId(),
                translation.getName(),
                translation.getDescription(),
                product.getPrice(),
                translation.getMaterial(),
                product.getSalePrice(),
                product.isOnSale(),
                calculateCurrentPrice(product),
                product.getStatus().name(),
                categoryName,
                getActivePromotionName(product, LanguageUtils.getCurrentLanguage()),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getVariants().stream()
                        .map(productVariantMapper::toResponse)
                        .toList()
        );
    }

    // Calculate the current price of the product based on active promotions and sale status
    public BigDecimal calculateCurrentPrice(Product product) {
        // Priority 1: Check for active promotion price (highest priority)
        BigDecimal promotionPrice = getActivePromotionPrice(product);
        if (promotionPrice != null) {
            return promotionPrice;
        }

        // Priority 2: Check if product is on sale and has salePrice (medium priority)
        if (product.isOnSale() && product.getSalePrice() != null) {
            return product.getSalePrice();
        }

        // Priority 3: Default to original price (lowest priority)
        return product.getPrice();
    }

    // Retrieve the active promotion price for the product, if any
    private BigDecimal getActivePromotionPrice(Product product) {
        BigDecimal bigDecimal=promotionProductRepository.findActivePromotionPrice(product.getId()).orElse(null);
        return bigDecimal;
    }

    // Retrieve the name of the active promotion for the product in the specified language
    private String getActivePromotionName(Product product, Language language) {
        return promotionProductRepository.findActivePromotionForProduct(product.getId())
                .map(promotionProduct -> {
                    Promotion promotion = promotionProduct.getPromotion();
                    return promotion.getTranslations().stream()
                            .filter(t -> t.getLanguage() == language)
                            .findFirst()
                            .map(PromotionTranslation::getName)
                            .orElseGet(() -> promotion.getTranslations().stream()
                                    .filter(t -> t.getLanguage() == Language.VI)
                                    .findFirst()
                                    .map(PromotionTranslation::getName)
                                    .orElse(null));
                })
                .orElse(null);
    }
}
