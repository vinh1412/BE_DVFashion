/*
 * @ {#} PromotionMapper.java   1.0     04/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.PromotionOrderResponse;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.entities.Promotion;
import vn.edu.iuh.fit.entities.PromotionTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.PromotionTranslationRepository;
import vn.edu.iuh.fit.utils.TextUtils;

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

        return new PromotionResponse(
                promotion.getId(),
                TextUtils.removeTrailingDot(translation.getName()),
                translation.getDescription(),
                promotion.getType(),
                promotion.getValue(),
                promotion.getMinOrderAmount(),
                promotion.getMaxUsages(),
                promotion.getCurrentUsages(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive()
        );
    }

    public PromotionOrderResponse mapPromotionOrderResponse(Promotion promotion, Language language) {
        String promotionName = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .map(PromotionTranslation::getName)
                .orElse("Unknown Promotion");

        return new PromotionOrderResponse(
                promotion.getId(),
                promotionName,
                promotion.getValue(),
                promotion.getType().name()
        );
    }
}
