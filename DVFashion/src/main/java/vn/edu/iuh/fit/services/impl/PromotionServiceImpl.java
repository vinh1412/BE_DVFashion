/*
 * @ {#} PromotionServiceImpl.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CreatePromotionRequest;
import vn.edu.iuh.fit.dtos.request.PromotionProductRequest;
import vn.edu.iuh.fit.dtos.request.UpdatePromotionProductRequest;
import vn.edu.iuh.fit.dtos.request.UpdatePromotionRequest;
import vn.edu.iuh.fit.dtos.response.PageResponse;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional
    @Override
    public PromotionResponse updatePromotion(UpdatePromotionRequest request, Long id, Language inputLang, MultipartFile bannerFile) {
        // Find existing promotion
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + id));

        // Validate dates
        if (request.startDate() != null && request.endDate() != null) {
            validatePromotionDates(request.startDate(), request.endDate());
        }

        // Validate products exist
        if (request.promotionProducts() != null && !request.promotionProducts().isEmpty()) {
            validateProductsExistForUpdate(request.promotionProducts());
        }

        // Handle banner file upload if provided
        if (bannerFile != null && !bannerFile.isEmpty()) {
            String bannerUrl = cloudinaryService.uploadImage(bannerFile);
            existingPromotion.setBannerUrl(bannerUrl);
        }

        // Update promotion basic fields
        if (request.type() != null) {
            existingPromotion.setType(PromotionType.valueOf(request.type()));
        }

        if (request.startDate() != null) {
            existingPromotion.setStartDate(LocalDate.parse(request.startDate()).atStartOfDay());
        }

        if (request.endDate() != null) {
            existingPromotion.setEndDate(LocalDate.parse(request.endDate()).atTime(23, 59, 59));
        }

        if (request.active() != null) {
            existingPromotion.setActive(request.active());

            if (!request.active()) {
                existingPromotion.getPromotionProducts()
                        .forEach(p -> p.setActive(false));
            }
        }

        // Update translations
        if (request.name() != null || request.description() != null) {
            updatePromotionTranslations(existingPromotion, inputLang, request.name(), request.description());
        }

        // Update promotion products
        if (request.promotionProducts() != null && !request.promotionProducts().isEmpty()) {
            updatePromotionProducts(existingPromotion, request.promotionProducts());
        }

        // Save updated promotion
        existingPromotion = promotionRepository.save(existingPromotion);

        return promotionMapper.mapToPromotionResponse(existingPromotion, inputLang);
    }

    @Override
    public PromotionResponse getPromotionById(Long id, Language language) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + id));

        return promotionMapper.mapToPromotionResponse(promotion, language);
    }

    @Override
    public List<PromotionResponse> getAllPromotions(Language language) {
        List<Promotion> promotions = promotionRepository.findAll();

        return promotions.stream()
                .map(promotion -> promotionMapper.mapToPromotionResponse(promotion, language))
                .toList();
    }

    @Override
    public PageResponse<PromotionResponse> getPromotionsPaging(Pageable pageable, Language language) {
        Page<Promotion> promotions = promotionRepository.findAll(pageable);

        Page<PromotionResponse> dtoPage = promotions.map(promotion ->
                promotionMapper.mapToPromotionResponse(promotion, language));

        return PageResponse.from(dtoPage);
    }

    @Override
    public void removeProductFromPromotion(Long promotionId, Long productId) {
        // Find existing promotion
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + promotionId));

        // Find the promotion product to remove
        PromotionProduct promotionProduct = promotion.getPromotionProducts().stream()
                .filter(pp -> pp.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found in promotion " + promotionId));

        // Remove from promotion's collection
        promotion.getPromotionProducts().remove(promotionProduct);

        // Save the updated promotion
        promotionRepository.save(promotion);
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

        // Check existence in DB
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

    // Update promotion translations based on input language
    private void updatePromotionTranslations(Promotion promotion, Language inputLang, String name, String description) {
        if (name == null && description == null) return;
        // Update existing translation or create new one
        PromotionTranslation existingTranslation = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == inputLang)
                .findFirst()
                .orElse(null);

        if (existingTranslation != null) {
            if (name != null) existingTranslation.setName(name);
            if (description != null) existingTranslation.setDescription(description);
        } else {
            promotion.getTranslations().add(buildTranslation(promotion, inputLang, name, description));
        }

        // Update or create translation for target language
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;
        String translatedName = (name != null) ? translationService.translate(name, targetLang.name()) : null;
        String translatedDescription = (description != null)
                ? translationService.translate(description, targetLang.name())
                : null;

        PromotionTranslation targetTranslation = promotion.getTranslations().stream()
                .filter(t -> t.getLanguage() == targetLang)
                .findFirst()
                .orElse(null);

        if (targetTranslation != null) {
            if (translatedName != null) targetTranslation.setName(translatedName);
            if (translatedDescription != null) targetTranslation.setDescription(translatedDescription);
        } else {
            promotion.getTranslations().add(buildTranslation(promotion, targetLang, translatedName, translatedDescription));
        }
    }

    // Update promotion products based on the provided requests
    private void updatePromotionProducts(Promotion promotion, List<UpdatePromotionProductRequest> productRequests) {
        // Get existing promotion products
        List<PromotionProduct> existingProducts = new ArrayList<>(promotion.getPromotionProducts());

        // Create maps for easy lookup
        Map<Long, PromotionProduct> existingById = existingProducts.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(PromotionProduct::getId, Function.identity()));

        // Create map by productId
        Map<Long, PromotionProduct> existingByProductId = existingProducts.stream()
                .filter(p -> p.getProduct() != null)
                .collect(Collectors.toMap(p -> p.getProduct().getId(), Function.identity()));

        // Prepare list for updated products
        List<PromotionProduct> updatedProducts = new ArrayList<>();

        for (UpdatePromotionProductRequest req : productRequests) {
            // Find product
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + req.productId()));

            // Determine original price
            BigDecimal originalPrice = (product.getSalePrice() != null && product.isOnSale())
                    ? product.getSalePrice()
                    : product.getPrice();

            // Get promotionPrice and discountPercentage from request
            BigDecimal promotionPrice = req.promotionPrice();
            BigDecimal discountPercentage = req.discountPercentage();

            PromotionProduct promotionProduct;

            // === (1) If ID exists, check existence and update ===
            if (req.id() != null) {
                // Check if PromotionProduct with given ID exists
                promotionProduct = existingById.get(req.id());
                if (promotionProduct == null) {
                    throw new ResourceNotFoundException("PromotionProduct not found with ID: " + req.id());
                }

                // Ensure the productId matches the existing PromotionProduct
                if (!promotionProduct.getProduct().getId().equals(req.productId())) {
                    throw new BadRequestException("Product ID: "+promotionProduct.getProduct().getId()+" does not match PromotionProductId: " + req.id());
                }

                // Update fields if provided
                if (promotionPrice != null || discountPercentage != null) {
                    if (promotionPrice == null) {
                        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 ||
                                discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                            throw new BadRequestException("Discount percentage must be between 0 and 100");
                        }
                        promotionPrice = originalPrice
                                .multiply(BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                                .setScale(2, RoundingMode.HALF_UP);
                    } else if (discountPercentage == null) {
                        if (promotionPrice.compareTo(originalPrice) >= 0) {
                            throw new BadRequestException("Promotion price must be less than original price");
                        }
                        discountPercentage = calculateDiscountPercentage(originalPrice, promotionPrice);
                    }

                    promotionProduct.setPromotionPrice(promotionPrice);
                    promotionProduct.setDiscountPercentage(discountPercentage);
                }

                promotionProduct.setOriginalPrice(originalPrice);

                if (req.stockQuantity() != null) {
                    promotionProduct.setStockQuantity(req.stockQuantity());
                }

                if (req.maxQuantityPerUser() != null) {
                    promotionProduct.setMaxQuantityPerUser(req.maxQuantityPerUser());
                }

                if (req.active() != null) {
                    promotionProduct.setActive(req.active());
                }

            }
            // === (2) If there is no ID, check for duplicate products ===
            else if (existingByProductId.containsKey(req.productId())) {
                throw new BadRequestException("Product with ID " + req.productId() + " already exists in this promotion");
            }
            // === (3) If it is a new product => create new ===
            else {
                // Validate that at least one of promotionPrice or discountPercentage is provided
                if (promotionPrice == null && discountPercentage == null) {
                    throw new BadRequestException("Either promotionPrice or discountPercentage must be provided");
                }

                // Validate required fields for new promotion product
                if(req.stockQuantity() == null) {
                    throw new BadRequestException("Stock quantity is required for new promotion product");
                }

                if (req.maxQuantityPerUser() == null) {
                    throw new BadRequestException("Max quantity per user is required for new promotion product");
                }

                // If you only enter discountPercentage → automatically calculate promotionPrice
                if (promotionPrice == null) {
                    if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 ||
                            discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                        throw new BadRequestException("Discount percentage must be between 0 and 100");
                    }
                    promotionPrice = originalPrice
                            .multiply(BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                            .setScale(2, RoundingMode.HALF_UP);
                } else if (discountPercentage == null) {
                    if (promotionPrice.compareTo(originalPrice) >= 0) {
                        throw new BadRequestException("Promotion price must be less than original price");
                    }
                    discountPercentage = calculateDiscountPercentage(originalPrice, promotionPrice);
                }

                promotionProduct = PromotionProduct.builder()
                        .promotion(promotion)
                        .product(product)
                        .originalPrice(originalPrice)
                        .promotionPrice(promotionPrice)
                        .discountPercentage(discountPercentage)
                        .stockQuantity(req.stockQuantity())
                        .soldQuantity(0)
                        .maxQuantityPerUser(req.maxQuantityPerUser())
                        .active(req.active() != null ? req.active() : true)
                        .build();
            }

            updatedProducts.add(promotionProduct);
        }

        promotion.getPromotionProducts().addAll(updatedProducts);
    }

    // Validate that all products in the update request exist
    private void validateProductsExistForUpdate(List<UpdatePromotionProductRequest> productRequests) {
        List<Long> productIds = new ArrayList<>(
                productRequests.stream()
                        .map(UpdatePromotionProductRequest::productId)
                        .toList());

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
}
