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
import vn.edu.iuh.fit.dtos.response.SizeResponse;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.Size;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.SizeMapper;
import vn.edu.iuh.fit.repositories.ProductVariantRepository;
import vn.edu.iuh.fit.repositories.SizeRepository;
import vn.edu.iuh.fit.services.SizeService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {
    private final SizeRepository sizeRepository;

    private final ProductVariantRepository productVariantRepository;

    private final SizeMapper sizeMapper;
    @Override
    public SizeResponse createSize(Long variantId, SizeRequest request) {
        // Check if ProductVariant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + variantId));

        // Check if Size with the same name already exists for the given ProductVariant
        boolean exists = sizeRepository.existsByProductVariantAndSizeName(variant, request.sizeName());

        if (exists) {
            throw new NotFoundException("Size already exists with name: " + request.sizeName());
        }

        // Create and save Size
        Size size = new Size();
        size.setProductVariant(variant);
        size.setSizeName(request.sizeName());
        size.setStockQuantity(request.stockQuantity());

        Size sizeResponse = sizeRepository.save(size);
        // return the size response
        return sizeMapper.toResponse(sizeResponse);
    }

    @Override
    public SizeResponse updateSize(Long sizeId, SizeRequest request) {
        // Check if Size exists
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new NotFoundException("Size not found with id: " + sizeId));


        if (request.sizeName() != null && !request.sizeName().isEmpty()) {
            // Check if Size with the same name already exists for the given ProductVariant
            boolean exists = sizeRepository.existsByProductVariantAndSizeName(size.getProductVariant(), request.sizeName());

            // If the name is changing, ensure it doesn't conflict with another existing size
            boolean isSameName = size.getSizeName().equals(request.sizeName());
            if (exists && !isSameName) {
                throw new NotFoundException("Size already exists with name: " + request.sizeName());
            }

            size.setSizeName(request.sizeName());
        }

        size.setStockQuantity(request.stockQuantity());

        // Save updated Size
        Size sizeResponse = sizeRepository.save(size);

        // return the size response
        return sizeMapper.toResponse(sizeResponse);
    }

    @Override
    public SizeResponse getSizeById(Long sizeId) {
        // Check if Size exists
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new NotFoundException("Size not found with id: " + sizeId));

        // Map entity to response DTO
        return sizeMapper.toResponse(size);
    }

    @Override
    public List<SizeResponse> getSizesByVariantId(Long variantId) {
        // Check if ProductVariant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Product variant not found with id: " + variantId));

        // Retrieve sizes by ProductVariant ID
        List<Size> sizes = sizeRepository.findByProductVariantId(variant.getId());

        // Map entities to response DTOs
        return sizes.stream()
                .map(sizeMapper::toResponse)
                .toList();
    }
}
