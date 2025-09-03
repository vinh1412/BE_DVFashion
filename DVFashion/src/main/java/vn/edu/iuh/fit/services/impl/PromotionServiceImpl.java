/*
 * @ {#} PromotionServiceImpl.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.PromotionRequest;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.entities.Promotion;
import vn.edu.iuh.fit.entities.PromotionTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.PromotionType;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.PromotionRepository;
import vn.edu.iuh.fit.repositories.PromotionTranslationRepository;
import vn.edu.iuh.fit.services.PromotionService;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.utils.TextUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * @description: Implementation of PromotionService interface for managing promotions.
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;

    private final PromotionTranslationRepository translationRepository;

    private final TranslationService translationService;

    @Override
    public PromotionResponse createPromotion(PromotionRequest promotionRequest, Language inputLang) {
        // Check if promotion with same name already exists
        if(translationRepository.existsByNameIgnoreCaseAndLanguage(promotionRequest.name().toLowerCase(), inputLang)) {
            throw new AlreadyExistsException("Promotion with name '" + promotionRequest.name() + "' already exists.");
        }

        // Parse and validate promotion type
        PromotionType type = PromotionType.fromString(promotionRequest.type());

        // Parse and validate dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(promotionRequest.startDate(), formatter);
        LocalDate endDate = LocalDate.parse(promotionRequest.endDate(), formatter);

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2025-09-05T00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 2025-09-10T23:59:59

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        // Create promotion entity
        Promotion promotion = Promotion.builder()
                .type(type)
                .value(promotionRequest.value())
                .minOrderAmount(promotionRequest.minOrderAmount())
                .maxUsages(promotionRequest.maxUsages() != null ? promotionRequest.maxUsages() : 0)
                .currentUsages(0)
                .startDate(startDateTime)
                .endDate(endDateTime)
                .active(promotionRequest.active() == null || promotionRequest.active())
                .build();
        promotionRepository.save(promotion);

        // Description for input language
        String descInput;
        if (promotionRequest.description() != null) {
            descInput = promotionRequest.description();
        } else {
            descInput = (inputLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        // Save input language translation
        PromotionTranslation inputTranslation = PromotionTranslation.builder()
                .promotion(promotion)
                .language(inputLang)
                .name(promotionRequest.name())
                .description(descInput)
                .build();
        translationRepository.save(inputTranslation);

        // Determine target language for translation
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        // Description for target language
        String descTarget;
        if (promotionRequest.description() != null) {
            descTarget = translationService.translate(promotionRequest.description(), targetLang.name());
        } else {
            descTarget = (targetLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        // Translate and save target language translation
        PromotionTranslation translatedTranslation = PromotionTranslation.builder()
                .promotion(promotion)
                .language(targetLang)
                .name(translationService.translate(promotionRequest.name(), targetLang.name()))
                .description(descTarget)
                .build();
        translationRepository.save(translatedTranslation);

        return toResponse(promotion, inputLang);
    }

    @Override
    public PromotionResponse getPromotionById(Long id, Language language) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));

        return toResponse(promotion, language);
    }

    private PromotionResponse toResponse(Promotion promotion, Language lang) {
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
}
