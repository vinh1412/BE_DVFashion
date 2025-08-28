/*
 * @ {#} SizeServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

/*
 * @description: Service implementation for managing Sizes
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.SizeRequest;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.Size;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
import vn.edu.iuh.fit.repositories.SizeRepository;
import vn.edu.iuh.fit.services.SizeService;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {
    private final SizeRepository sizeRepository;

    private final ProductVariantRepository productVariantRepository;

    @Override
    public Size createSize(Long variantId, SizeRequest request) {
        // Check if ProductVariant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + variantId));

        // Create and save Size
        Size size = new Size();
        size.setProductVariant(variant);
        size.setSizeName(request.sizeName());
        size.setStockQuantity(request.stockQuantity());

        // return the size entity
        return sizeRepository.save(size);
    }
}
