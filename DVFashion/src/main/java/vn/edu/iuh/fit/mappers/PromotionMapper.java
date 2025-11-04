/*
 * @ {#} PromotionMapper.java   1.0     04/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.PromotionOrderResponse;
import vn.edu.iuh.fit.dtos.response.PromotionProductResponse;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.PromotionTranslationRepository;
import vn.edu.iuh.fit.utils.LanguageUtils;
import vn.edu.iuh.fit.utils.TextUtils;

import java.util.List;

/*
 * @description: Mapper class for converting between Promotion entities and DTOs
 * @author: Tran Hien Vinh
 * @date:   04/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class PromotionMapper {
    private final PromotionTranslationRepository translationRepository;

    public PromotionResponse toResponse(Promotion promotion, Language lang) {
        PromotionTranslation translation = translationRepository
                .findByPromotionIdAndLanguage(promotion.getId(), lang)
                .orElseGet(() -> translationRepository
                        .findByPromotionIdAndLanguage(promotion.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("No translation found"))
                );

        return null;

//        return new PromotionResponse(
//                promotion.getId(),
//                TextUtils.removeTrailingDot(translation.getName()),
//                translation.getDescription(),
//                promotion.getType(),
//                promotion.getValue(),
//                promotion.getMinOrderAmount(),
//                promotion.getMaxUsages(),
//                promotion.getCurrentUsages(),
//                promotion.getStartDate(),
//                promotion.getEndDate(),
//                promotion.isActive()
//        );
    }

    public PromotionOrderResponse mapPromotionOrderResponse(Promotion promotion, Language language) {
        String promotionName = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .map(PromotionTranslation::getName)
                .orElse("Unknown Promotion");

        return null;

//        return new PromotionOrderResponse(
//                promotion.getId(),
//                promotionName,
//                promotion.getValue(),
//                promotion.getType().name()
//        );
    }

    public PromotionResponse mapToPromotionResponse(Promotion promotion, Language language) {
        PromotionTranslation translation = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElse(promotion.getTranslations().get(0));

        List<PromotionProductResponse> productResponses = promotion.getPromotionProducts().stream()
                .map(this::mapToPromotionProductResponse)
                .toList();

        return new PromotionResponse(
                promotion.getId(),
                translation.getName(),
                translation.getDescription(),
                promotion.getType(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getBannerUrl(),
                promotion.isActive(),
                productResponses
        );
    }

    private PromotionProductResponse mapToPromotionProductResponse(PromotionProduct promotionProduct) {
        return new PromotionProductResponse(
                promotionProduct.getId(),
                promotionProduct.getProduct().getId(),
                getProductName(promotionProduct.getProduct()),
                promotionProduct.getOriginalPrice(),
                promotionProduct.getPromotionPrice(),
                promotionProduct.getDiscountPercentage(),
                promotionProduct.getStockQuantity(),
                promotionProduct.getSoldQuantity(),
                promotionProduct.getMaxQuantityPerUser(),
                promotionProduct.isActive()
        );
    }

    private String getProductName(Product product) {
        Language lang = LanguageUtils.getCurrentLanguage();
        return product.getTranslations().stream()
                .filter(t -> t.getLanguage() == lang)
                .map(ProductTranslation::getName)
                .findFirst()
                .orElseGet(() -> getFallbackName(product, lang));
    }

    private String getFallbackName(Product product, Language lang) {
        // fallback: return name in Vietnamese or default message
        return product.getTranslations().stream()
                .map(ProductTranslation::getName)
                .findFirst()
                .orElse(lang == Language.VI ? "Không có tên sản phẩm" : "Unknown Product");
    }
}
