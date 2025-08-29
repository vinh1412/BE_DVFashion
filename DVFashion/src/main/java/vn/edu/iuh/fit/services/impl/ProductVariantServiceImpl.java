/*
 * @ {#} ProductVariantServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;
import vn.edu.iuh.fit.dtos.response.ProductVariantResponse;
import vn.edu.iuh.fit.dtos.response.SizeResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.entities.Size;
import vn.edu.iuh.fit.enums.ProductVariantStatus;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.ProductVariantMapper;
import vn.edu.iuh.fit.mappers.SizeMapper;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.ProductVariantImageRepository;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
import vn.edu.iuh.fit.repositories.SizeRepository;
import vn.edu.iuh.fit.services.ProductVariantImageService;
import vn.edu.iuh.fit.services.ProductVariantService;
import vn.edu.iuh.fit.services.SizeService;

import java.util.List;

/*
 * @description: Service implementation for managing Product Variants
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;

    private final ProductVariantImageService productVariantImageService;

    private final SizeService sizeService;

    private final ProductRepository productRepository;

    private final ProductVariantMapper productVariantMapper;

    private final SizeRepository sizeRepository;

    private final ProductVariantImageRepository productVariantImageRepository;


    @Transactional
    @Override
    public ProductVariantResponse createProductVariant(Long productId, ProductVariantRequest request, List<MultipartFile> variantImages) {
        // Check if Product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Check for duplicate color within the same product
        boolean exists = productVariantRepository.existsByProductIdAndColorIgnoreCase(productId, request.color());
        if (exists) {
            throw new AlreadyExistsException("A variant with color '" + request.color() + "' already exists for this product.");
        }

        // Create and save ProductVariant
        ProductVariant productVariant = new ProductVariant();
        productVariant.setProduct(product);
        productVariant.setColor(request.color());
        productVariant.setAddtionalPrice(request.additionalPrice());
        productVariant.setStatus(ProductVariantStatus.valueOf(request.status()));

        productVariantRepository.save(productVariant);

        // Validate sizes
        if (request.sizes() == null || request.sizes().isEmpty()) {
            throw new IllegalArgumentException("At least one size must be provided for the product variant.");
        }

        // Save Sizes
        for (SizeRequest s : request.sizes()) {
            SizeResponse sizeResponse=sizeService.createSize(productVariant.getId(), s);

            Size size = sizeRepository.findById(sizeResponse.id())
                    .orElseThrow(() -> new NotFoundException("Size not found with id: " + sizeResponse.id()));
            productVariant.getSizes().add(size);
        }

        // Validate images
        if (request.images() == null || request.images().isEmpty()) {
            throw new IllegalArgumentException("At least one image must be provided for the product variant.");
        }

        // Save Images
        for (int i = 0; i < request.images().size(); i++) {
            // Get corresponding image file if available
            ProductVariantImageRequest imgReq = request.images().get(i);

            // Get the corresponding MultipartFile if available
            MultipartFile imageFile = (variantImages != null && i < variantImages.size())
                    ? variantImages.get(i)
                    : null;

            ProductVariantImageResponse productVariantImageResponse = productVariantImageService.addImageToVariant(productVariant.getId(), imgReq, imageFile);

            ProductVariantImage image = productVariantImageRepository.findById(productVariantImageResponse.id())
                    .orElseThrow(() -> new NotFoundException("Product variant image not found with id: " + productVariantImageResponse.id()));

            productVariant.getImages().add(image);
        }
        product.getVariants().add(productVariant);

        // Save the updated ProductVariant
        productVariantRepository.save(productVariant);

        // Map to Response DTO
        return productVariantMapper.toResponse(productVariant);
    }

    @Override
    public ProductVariantResponse updateProductVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant productVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Product variant not found with id: " + variantId));

        // Update basic variant properties
        if (request.color() != null) {
            // Check for duplicate color within the same product
            boolean exists = productVariantRepository.existsByProductIdAndColorIgnoreCase(
                    productVariant.getProduct().getId(), request.color());

            boolean isSameColor = productVariant.getColor().equalsIgnoreCase(request.color());

            if (exists && !isSameColor) {
                throw new AlreadyExistsException("A variant with color '" + request.color() + "' already exists for this product.");
            }
            productVariant.setColor(request.color());
        }

        // Update additional price if provided
        if (request.additionalPrice() != null) {
            productVariant.setAddtionalPrice(request.additionalPrice());
        }

        // Update status if provided
        if (request.status() != null) {
            productVariant.setStatus(ProductVariantStatus.valueOf(request.status()));
        }

        productVariantRepository.save(productVariant);
        return productVariantMapper.toResponse(productVariant);
    }

    @Override
    public ProductVariantResponse getProductVariantById(Long variantId) {
        // Retrieve ProductVariant by ID
        ProductVariant productVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Product variant not found with id: " + variantId));

        // Map to Response DTO
        return productVariantMapper.toResponse(productVariant);
    }
}
