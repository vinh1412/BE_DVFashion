/*
 * @ {#} ProductServiceImpl.java   1.0     28/08/2025
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
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.InteractionType;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.ResourceNotFoundException;
import vn.edu.iuh.fit.mappers.ProductMapper;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.services.*;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Service implementation for managing Products
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final PromotionRepository promotionRepository;

    private final ProductTranslationService productTranslationService;

    private final ProductVariantService productVariantService;

    private final CategoryService categoryService;

    private final ProductTranslationRepository productTranslationRepository;

    private final ProductMapper productMapper;

    private final PromotionService promotionService;

    private final UserService userService;

    private final UserInteractionService userInteractionService;

    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request, Language inputLang, List<MultipartFile> variantImages) {
        // Check if Category exists
        Category category= categoryRepository.findById(request.categoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        // Check if Promotion exists (if provided)
        Promotion promotion = null;
        if (request.promotionId() != null) {
            promotion = promotionRepository.findById(request.promotionId())
                    .orElseThrow(() -> new NotFoundException("Promotion not found"));
        }

        // Create and save the Product entity
        Product product = new Product();
        product.setCategory(category);
        product.setPromotion(promotion);
        product.setPrice(request.price());
        product.setSalePrice(request.salePrice());
        product.setOnSale(request.onSale());
        product.setStatus(ProductStatus.valueOf(request.status()));

        productRepository.save(product);

        // Create and save ProductTranslation
        productTranslationService.createProductTranslations(product, request, inputLang);

        // if no variants provided, throw exception
        if (request.variants() == null || request.variants().isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one variant");
        }


        // Tick off images for each variant
        int imageIndex = 0;

        // Total uploaded images
        int totalUploadedImages = (variantImages != null) ? variantImages.size() : 0;

        for (ProductVariantRequest v : request.variants()) {
            // Determine how many images to assign to this variant
            int requestedImageCount = (v.images() != null) ? v.images().size() : 0;
            // Calculate how many images are still available to assign
            int availableImages = Math.max(0, totalUploadedImages - imageIndex);
            // Actual number of images to assign to this variant
            int actualImageCount = Math.min(requestedImageCount, availableImages);

            List<MultipartFile> variantImageFiles;
            if (actualImageCount > 0) {
                variantImageFiles = variantImages.subList(imageIndex, imageIndex + actualImageCount);
                imageIndex += actualImageCount;
            } else {
                variantImageFiles = List.of();
            }

            // Create ProductVariant
            productVariantService.createProductVariant(product.getId(), v, variantImageFiles);
        }

        // Return product with translations
        return toResponse(product, inputLang);
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request, Language inputLang) {
        // Find existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Check if Category exists
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            product.setCategory(category);
        }

        // Check if Promotion exists (if provided)
        if (request.promotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.promotionId())
                    .orElseThrow(() -> new NotFoundException("Promotion not found"));
            product.setPromotion(promotion);
        }

        // Update product basic properties
        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.salePrice() != null) {
            product.setSalePrice(request.salePrice());
        }

        product.setOnSale(request.onSale());


        if (request.status() != null) {
            product.setStatus(ProductStatus.valueOf(request.status()));
        }

        productRepository.save(product);

        // Update ProductTranslation
        productTranslationService.updateProductTranslations(product, request, inputLang);

        return toResponse(product, inputLang);
    }

    @Override
    public ProductResponse getProductById(Long productId, Language language) {
        // Find existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Track user interaction
        try {
            Long currentUserId = userService.getCurrentUser().getId();
            if (currentUserId != null) {
                userInteractionService.trackInteraction(currentUserId, productId, InteractionType.VIEW, null);
            } else {
                // If no user logged in, skip tracking or handle as guest
                log.info("Guest viewed product {}, not tracking to DB yet", productId);
            }
        } catch (Exception e) {
            log.error("Failed to track interaction for product {}: {}", productId, e.getMessage());
        }

        // Map to ProductResponse
        return toResponse(product, language);
    }

    @Override
    public List<ProductResponse> getAllProducts(Language language) {
        List<Product> products = productRepository.findAll();

        // Map each product to ProductResponse
        return products.stream()
                .map(product -> toResponse(product, language))
                .toList();
    }

    @Override
    public PageResponse<ProductResponse> getProductsPaging(Pageable pageable, Language language) {
        Page<Product> productPage = productRepository.findAll(pageable);

        Page<ProductResponse> productResponses = productPage.map(product -> toResponse(product, language));

        return PageResponse.from(productResponses);
    }

    @Override
    public List<ProductResponse> getProductsByPromotionId(Long promotionId, Language language) {
        // Check if promotion exists
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + promotionId));

        // Get all products from promotion products
        List<Product> products = promotion.getPromotionProducts().stream()
                .map(PromotionProduct::getProduct)
                .toList();

        // Map each product to ProductResponse
        return products.stream()
                .map(product -> toResponse(product, language))
                .toList();
    }

    @Override
    public PageResponse<ProductResponse> getProductsByPromotionIdPaging(Long promotionId, Pageable pageable, Language language) {
        // Check if promotion exists
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + promotionId));

        // Get products with pagination through PromotionProduct relationship
        Page<Product> products = productRepository.findProductsByPromotionId(promotion.getId(), pageable);

        // Map each product to ProductResponse
        Page<ProductResponse> productResponses = products.map(product -> toResponse(product, language));

        return PageResponse.from(productResponses);
    }

    @Override
    public List<ProductResponse> getProductsByCategoryId(Long categoryId, Language language) {
        // Check if category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Get all products by category
        List<Product> products = productRepository.findByCategoryId(category.getId());

        // Map each product to ProductResponse
        return products.stream()
                .map(product -> toResponse(product, language))
                .toList();
    }

    @Override
    public PageResponse<ProductResponse> getProductsByCategoryIdPaging(Long categoryId, Pageable pageable, Language language) {
        // Check if category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Get products with pagination by category
        Page<Product> products = productRepository.findByCategoryId(category.getId(), pageable);

        // Map each product to ProductResponse
        Page<ProductResponse> productResponses = products.map(product -> toResponse(product, language));

        return PageResponse.from(productResponses);
    }

    private ProductResponse toResponse(Product product, Language inputLang) {
        // Find translation in requested language, if not found, fallback to Vietnamese
        ProductTranslation translation = productTranslationRepository.findByProductIdAndLanguage(product.getId(), inputLang)
                .orElseGet(() -> productTranslationRepository.findByProductIdAndLanguage(product.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("Translation not found for language: " + inputLang)));

        // Find category
        CategoryResponse categoryResponse = categoryService.getCategoryById(product.getCategory().getId(), inputLang);
        String categoryName = categoryResponse != null ? categoryResponse.name() : "Unknown";

        // Map to ProductResponse
        return productMapper.toResponse(product, translation, categoryName);
    }
}
