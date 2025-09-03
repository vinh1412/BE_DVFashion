/*
 * @ {#} PromotionServiceImpl.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.PromotionRequest;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.entities.Promotion;
import vn.edu.iuh.fit.entities.PromotionTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.PromotionType;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.PromotionMapper;
import vn.edu.iuh.fit.repositories.PromotionRepository;
import vn.edu.iuh.fit.repositories.PromotionTranslationRepository;
import vn.edu.iuh.fit.services.PromotionService;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.utils.TextUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private final PromotionMapper promotionMapper;

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

        return promotionMapper.toResponse(promotion, inputLang);
    }

    @Override
    public PromotionResponse getPromotionById(Long id, Language language) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));

        return promotionMapper.toResponse(promotion, language);
    }

    @Override
    public PromotionResponse updatePromotion(PromotionRequest promotionRequest, Long id, Language inputLang) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));

        // Update fields if provided
        if (promotionRequest.type() != null && !promotionRequest.type().isBlank()) {
            promotion.setType(PromotionType.fromString(promotionRequest.type()));
        }

        if (promotionRequest.value() != null) {
            promotion.setValue(promotionRequest.value());
        }

        if (promotionRequest.minOrderAmount() != null) {
            promotion.setMinOrderAmount(promotionRequest.minOrderAmount());
        }

        if (promotionRequest.maxUsages() != null) {
            promotion.setMaxUsages(promotionRequest.maxUsages());
        }

        // Parse and validate dates if provided
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (promotionRequest.startDate() != null) {
            LocalDate startDate = LocalDate.parse(promotionRequest.startDate(), formatter);
            LocalDateTime startDateTime = startDate.atStartOfDay();
            promotion.setStartDate(startDateTime);
        }

        if (promotionRequest.endDate() != null) {
            LocalDate endDate = LocalDate.parse(promotionRequest.endDate(), formatter);
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            promotion.setEndDate(endDateTime);
        }

        if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
            if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date.");
            }
        }


        if (promotionRequest.active() != null) {
            promotion.setActive(promotionRequest.active());
        }

        // Update or create translation for the input language
        updateOrCreateTranslation(promotion, inputLang, promotionRequest.name(), promotionRequest.description());

        // Determine the other language
        Language otherLang = (inputLang == Language.EN) ? Language.VI : Language.EN;

        String otherName = promotionRequest.name() != null
                ? translationService.translate(promotionRequest.name(), otherLang.name())
                : null;

        String otherDesc = promotionRequest.description() != null
                ? translationService.translate(promotionRequest.description(), otherLang.name())
                : null;

        // Update translations for the other language
        updateOrCreateTranslation(promotion, otherLang, otherName, otherDesc);

        // Save the updated promotion
        promotionRepository.save(promotion);

        return promotionMapper.toResponse(promotion, inputLang);
    }

    @Override
    public List<PromotionResponse> getAllPromotions(Language language) {
        List<Promotion> promotions = promotionRepository.findAll();

        return promotions.stream()
                .map(promotion -> promotionMapper.toResponse(promotion, language))
                .toList();
    }

    // Helper method to update or create a translation
    private void updateOrCreateTranslation(Promotion promotion, Language lang, String name, String description) {
        // Find existing translation or create a new one
        PromotionTranslation translation = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == lang)
                .findFirst()
                .orElseGet(() -> {
                    PromotionTranslation t = PromotionTranslation.builder()
                            .language(lang)
                            .promotion(promotion)
                            .build();
                    promotion.getTranslations().add(t);
                    return t;
                });

        // Update fields if new values are provided
        if (name != null && !name.isBlank()) {
            translation.setName(name);
        }
        if (description != null && !description.isBlank()) {
            translation.setDescription(description);
        }
    }
}
