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
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductVariantImageRepository;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.ProductVariantImageService;
import vn.edu.iuh.fit.utils.ImageUtils;

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

    @Transactional
    @Override
    public ProductVariantImage addImageToVariant(Long variantId, ProductVariantImageRequest request, MultipartFile imageFile) {
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

        // Save and return the image entity
        return productVariantImageRepository.save(productVariantImage);
    }
}
