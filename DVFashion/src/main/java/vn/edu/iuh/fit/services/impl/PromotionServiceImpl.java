/*
 * @ {#} PromotionServiceImpl.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CreatePromotionRequest;
import vn.edu.iuh.fit.dtos.request.PromotionProductRequest;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.PromotionType;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.ResourceNotFoundException;
import vn.edu.iuh.fit.mappers.PromotionMapper;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.PromotionRepository;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.PromotionService;
import vn.edu.iuh.fit.services.TranslationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * @description: Service implementation for managing promotions.
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;

    private final ProductRepository productRepository;

    private final PromotionMapper promotionMapper;

    private final TranslationService translationService;

    private final CloudinaryService cloudinaryService;

    @Transactional
    @Override
    public PromotionResponse createPromotion(CreatePromotionRequest request, Language inputLang, MultipartFile bannerFile) {
        // Validate dates
        validatePromotionDates(request.startDate(), request.endDate());

        // Validate products exist
        validateProductsExist(request.promotionProducts());

        // Handle banner file upload if provided (omitted for brevity)
        String bannerUrl = null;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            bannerUrl = cloudinaryService.uploadImage(bannerFile);
        }

        // Create promotion entity
        Promotion promotion = Promotion.builder()
                .type(PromotionType.valueOf(request.type()))
                .bannerUrl(bannerUrl)
                .startDate(LocalDate.parse(request.startDate()).atStartOfDay())
                .endDate(LocalDate.parse(request.endDate()).atTime(23, 59, 59))
                .active(request.active() != null ? request.active() : true)
                .promotionProducts(new ArrayList<>())
                .translations(new ArrayList<>())
                .build();

        // Save promotion first to get ID
        promotion = promotionRepository.save(promotion);

        promotion.getTranslations().add(buildTranslation(promotion, inputLang, request.name(), request.description()));

        // Determine the target language
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        // Use TranslationService to translate name & description
        String translatedName = translationService.translate(request.name(), targetLang.name());
        String translatedDescription = (request.description() != null)
                ? translationService.translate(request.description(), targetLang.name())
                : null;

        promotion.getTranslations().add(buildTranslation(promotion, targetLang, translatedName, translatedDescription));

        // Create promotion products
        for (PromotionProductRequest productRequest : request.promotionProducts()) {
            Product product = productRepository.findById(productRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productRequest.productId()));

            BigDecimal originalPrice = (product.getSalePrice() != null && product.isOnSale())
                    ? product.getSalePrice()
                    : product.getPrice();
            BigDecimal promotionPrice = productRequest.promotionPrice();
            BigDecimal discountPercentage = productRequest.discountPercentage();

            // Validate that at least one of promotionPrice or discountPercentage is provided
            if (promotionPrice == null && discountPercentage == null) {
                throw new BadRequestException("Either promotionPrice or discountPercentage must be provided");
            }

            // If you only enter discountPercentage → automatically calculate promotionPrice
            if (promotionPrice == null && discountPercentage != null) {
                if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new BadRequestException("Discount percentage must be between 0 and 100");
                }
                promotionPrice = originalPrice
                        .multiply(BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // If you only enter promotionPrice → automatically calculate discountPercentage
            else if (promotionPrice != null && discountPercentage == null) {
                if (promotionPrice.compareTo(originalPrice) >= 0) {
                    throw new BadRequestException("Promotion price must be less than original price");
                }
                discountPercentage = calculateDiscountPercentage(originalPrice, promotionPrice);
            }

            // If you enter both → check if they match
            else if (promotionPrice != null && discountPercentage != null) {
                BigDecimal expectedDiscount = calculateDiscountPercentage(originalPrice, promotionPrice);
                if (expectedDiscount.subtract(discountPercentage).abs().compareTo(BigDecimal.valueOf(1)) > 0) {
                    log.warn("Discount percentage and promotion price mismatch for product {}", productRequest.productId());
                }
            }

            PromotionProduct promotionProduct = PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .originalPrice(originalPrice)
                    .promotionPrice(promotionPrice)
                    .discountPercentage(discountPercentage)
                    .stockQuantity(productRequest.stockQuantity())
                    .soldQuantity(0)
                    .maxQuantityPerUser(productRequest.maxQuantityPerUser())
                    .active(true)
                    .build();

            promotion.getPromotionProducts().add(promotionProduct);
        }

        // Save final promotion with all relationships
        promotion = promotionRepository.save(promotion);

        return promotionMapper.mapToPromotionResponse(promotion, inputLang);
    }

    // Helper method to build PromotionTranslation
    private PromotionTranslation buildTranslation(Promotion promotion, Language lang, String name, String description) {
        return PromotionTranslation.builder()
                .promotion(promotion)
                .language(lang)
                .name(name)
                .description(description)
                .build();
    }

    // Validate promotion start and end dates
    private void validatePromotionDates(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    // Validate that all products in the request exist
    private void validateProductsExist(List<PromotionProductRequest> productRequests) {
        List<Long> productIds = new ArrayList<>(
                productRequests.stream()
                        .map(PromotionProductRequest::productId)
                        .toList()
        );

        // Check for duplicate product IDs
        Set<Long> uniqueIds = new HashSet<>();
        for (Long id : productIds) {
            if (!uniqueIds.add(id)) {
                throw new BadRequestException("Duplicate productId found: " + id);
            }
        }


        List<Long> existingProductIds = productRepository.findExistingProductIds(productIds);

        if (existingProductIds.size() != productIds.size()) {
            productIds.removeAll(existingProductIds);
            throw new ResourceNotFoundException("Products not found with IDs: " + productIds);
        }
    }

    // Calculate discount percentage based on original and promotion prices
    private BigDecimal calculateDiscountPercentage(BigDecimal originalPrice, BigDecimal promotionPrice) {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = originalPrice.subtract(promotionPrice);
        return discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
