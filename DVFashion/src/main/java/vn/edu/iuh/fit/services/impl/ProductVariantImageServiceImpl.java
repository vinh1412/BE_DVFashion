/*
 * @ {#} ProductVariantImageServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.dtos.response.ProductVariantImageResponse;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.ProductVariantImageMapper;
import vn.edu.iuh.fit.repositories.ProductVariantImageRepository;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.ProductVariantImageService;
import vn.edu.iuh.fit.utils.ImageUtils;

import java.util.List;

/*
 * @description: Service implementation for managing product variant images
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class ProductVariantImageServiceImpl implements ProductVariantImageService {
    private final ProductVariantImageRepository productVariantImageRepository;

    private final ProductVariantRepository productVariantRepository;

    private final CloudinaryService cloudinaryService;

    private final ProductVariantImageMapper productVariantImageMapper;

    @Transactional
    @Override
    public ProductVariantImageResponse addImageToVariant(Long variantId, ProductVariantImageRequest request, MultipartFile imageFile) {
        // Check if ProductVariant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Product variant not found with id: " + variantId));

        // Upload image and get URL
        String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);

        // Create and save ProductVariantImage
        ProductVariantImage productVariantImage = new ProductVariantImage();
        productVariantImage.setProductVariant(variant);
        productVariantImage.setImageUrl(imageUrl);
        productVariantImage.setPrimary(request.isPrimary());
        productVariantImage.setSortOrder(request.sortOrder());

        ProductVariantImage savedImage = productVariantImageRepository.save(productVariantImage);
        // Save and return the image response
        return productVariantImageMapper.toResponse(savedImage);
    }

    @Override
    public ProductVariantImageResponse updateVariantImage(Long imageId, ProductVariantImageRequest request, MultipartFile imageFile) {
        ProductVariantImage image = productVariantImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Product variant image not found with id: " + imageId));

        // Update image if new file is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);
            image.setImageUrl(newImageUrl);
        }

        // Update other fields if provided
        if (request != null) {
            if (request.isPrimary() != null) {
                image.setPrimary(request.isPrimary());
            }

            if (request.sortOrder() != null) {
                image.setSortOrder(request.sortOrder());
            }
        }

        ProductVariantImage updatedImage = productVariantImageRepository.save(image);

        // Save and return the updated image response
        return productVariantImageMapper.toResponse(updatedImage);
    }

    @Override
    public ProductVariantImageResponse getImageById(Long imageId) {
        // Retrieve image by ID
        ProductVariantImage image = productVariantImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Product variant image not found with id: " + imageId));

        // Map entity to response DTO
        return productVariantImageMapper.toResponse(image);
    }

    @Override
    public List<ProductVariantImageResponse> getImagesByVariantId(Long variantId) {
        // Check if ProductVariant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Product variant not found with id: " + variantId));

        // Retrieve images by ProductVariant ID
        List<ProductVariantImage> images = productVariantImageRepository.findByProductVariantId(variant.getId());

        // Map entities to response DTOs
        return images.stream()
                .map(productVariantImageMapper::toResponse)
                .toList();
    }
}
