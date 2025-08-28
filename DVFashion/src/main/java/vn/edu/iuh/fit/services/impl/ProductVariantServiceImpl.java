/*
 * @ {#} ProductVariantServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductVariantImageRequest;
import vn.edu.iuh.fit.dtos.request.ProductVariantRequest;
import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.dtos.response.ProductVariantResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.ProductVariantImage;
import vn.edu.iuh.fit.entities.Size;
import vn.edu.iuh.fit.enums.ProductVariantStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.ProductVariantMapper;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
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

    @Transactional
    @Override
    public ProductVariantResponse createProductVariant(Long productId, ProductVariantRequest request, List<MultipartFile> variantImages) {
        // Check if Product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Create and save ProductVariant
        ProductVariant productVariant = new ProductVariant();
        productVariant.setProduct(product);
        productVariant.setColor(request.color());
        productVariant.setAddtionalPrice(request.additionalPrice());
        productVariant.setStatus(ProductVariantStatus.valueOf(request.status()));

        productVariantRepository.save(productVariant);

        // Save Sizes
        if (request.sizes() != null && !request.sizes().isEmpty()) {
            for (SizeRequest s : request.sizes()) {
                Size size=sizeService.createSize(productVariant.getId(), s);
                productVariant.getSizes().add(size);
            }
        }

        // Save Images
        if (request.images() != null && !request.images().isEmpty()) {
            for (int i = 0; i < request.images().size(); i++) {
                ProductVariantImageRequest imgReq = request.images().get(i);
                MultipartFile imageFile = (variantImages != null && i < variantImages.size())
                        ? variantImages.get(i)
                        : null;
                ProductVariantImage image = productVariantImageService.addImageToVariant(productVariant.getId(), imgReq, imageFile);
                productVariant.getImages().add(image);
            }
        }

        // Update the product to include the new variant
        product.getVariants().add(productVariant);

        // Save the updated ProductVariant
        productVariantRepository.save(productVariant);

        // Map to Response DTO
        return productVariantMapper.toResponse(productVariant);
    }
}
