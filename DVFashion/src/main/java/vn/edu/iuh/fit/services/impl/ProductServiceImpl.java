/*
 * @ {#} ProductServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.ProductMapper;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.services.*;

import java.util.List;

/*
 * @description: Service implementation for managing Products
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;

    private final PromotionRepository promotionRepository;

    private final ProductTranslationService productTranslationService;

    private final ProductVariantService productVariantService;

    private final CategoryService categoryService;

    private final BrandService brandService;

    private final ProductTranslationRepository productTranslationRepository;

    private final ProductMapper productMapper;

    private final PromotionService promotionService;

    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request, Language inputLang, List<MultipartFile> variantImages) {
        // Check if Category exists
        Category category= categoryRepository.findById(request.categoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));
        // Check if Brand exists
        Brand brand= brandRepository.findById(request.brandId())
                .orElseThrow(()-> new NotFoundException("Brand not found"));

        // Check if Promotion exists (if provided)
        Promotion promotion = null;
        if (request.promotionId() != null) {
            promotion = promotionRepository.findById(request.promotionId())
                    .orElseThrow(() -> new NotFoundException("Promotion not found"));
        }

        // Create and save the Product entity
        Product product = new Product();
        product.setCategory(category);
        product.setBrand(brand);
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
        for (ProductVariantRequest v : request.variants()) {
            // Count images for this variant
            int imgCount = (v.images() != null) ? v.images().size() : 0;

            // Get the correct number of images belonging to the current variant from the list variantImages
            List<MultipartFile> variantImageFiles =
                    (variantImages != null && imageIndex + imgCount <= variantImages.size()) // If file uploaded and enough photos in the list
                    ? variantImages.subList(imageIndex, imageIndex + imgCount) // Get the sublist for this variant
                    : List.of();

            // Create ProductVariant
            productVariantService.createProductVariant(product.getId(), v, variantImageFiles);
            imageIndex += imgCount;
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

        // Check if Brand exists
        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new NotFoundException("Brand not found"));
            product.setBrand(brand);
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

    private ProductResponse toResponse(Product product, Language inputLang) {
        // Find translation in requested language, if not found, fallback to Vietnamese
        ProductTranslation translation = productTranslationRepository.findByProductIdAndLanguage(product.getId(), inputLang)
                .orElseGet(() -> productTranslationRepository.findByProductIdAndLanguage(product.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("Translation not found for language: " + inputLang)));

        // Find category
        CategoryResponse categoryResponse = categoryService.getCategoryById(product.getCategory().getId(), inputLang);
        String categoryName = categoryResponse != null ? categoryResponse.name() : "Unknown";

        // Find brand name
        BrandResponse brandResponse = brandService.getBrandById(product.getBrand().getId(), inputLang);
        String brandName = brandResponse != null ? brandResponse.name() : "Unknown";

        // Find promotion name if exists
        PromotionResponse promotionResponse = promotionService.getPromotionById(product.getPromotion().getId(), inputLang);
        String promotionName = promotionResponse != null ? promotionResponse.name() : null;

        // Map to ProductResponse
        return productMapper.toResponse(product, translation, categoryName, brandName, promotionName);
    }
}
