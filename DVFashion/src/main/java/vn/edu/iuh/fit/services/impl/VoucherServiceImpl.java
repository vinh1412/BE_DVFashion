/*
 * @ {#} VoucherServiceImpl.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.Voucher;
import vn.edu.iuh.fit.entities.VoucherProduct;
import vn.edu.iuh.fit.entities.VoucherTranslation;
import vn.edu.iuh.fit.enums.DiscountType;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.enums.VoucherType;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.ResourceNotFoundException;
import vn.edu.iuh.fit.mappers.VoucherMapper;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.VoucherProductRepository;
import vn.edu.iuh.fit.repositories.VoucherRepository;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.services.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * @description: Service implementation for Voucher operations
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;

    private final ProductRepository productRepository;

    private final TranslationService translationService;

    private final VoucherProductRepository voucherProductRepository;

    private final VoucherMapper voucherMapper;

    @Transactional
    @Override
    public VoucherResponse createVoucher(CreateVoucherRequest request, Language language) {
        // Validate voucher code uniqueness
        validateVoucherCode(request.code());

        // Validate dates
        validateDates(request.startDate(), request.endDate());

        // Validate discount configuration
        validateDiscountConfiguration(request);

        // Validate usage configuration
        validateUsageConfiguration(request);

        // Validate products if voucher is product-specific
        validateProducts(request);

        // Create voucher entity
        Voucher voucher = buildVoucher(request, language);;

        // Create voucher products if product-specific
        if (VoucherType.PRODUCT_SPECIFIC.name().equals(request.voucherType())) {
            createVoucherProducts(voucher, request.productIds());
        }

        return voucherMapper.mapToResponse(voucher, language);
    }

    // Validation code
    private void validateVoucherCode(String code) {
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new AlreadyExistsException("Voucher code '" + code + "' already exists");
        }
    }

    // Validate start and end dates
    private void validateDates(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDate today = LocalDate.now();

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        if (endDate.isBefore(today)) {
            throw new BadRequestException("End date cannot be in the past");
        }
    }

    // Validate discount configuration
    private void validateDiscountConfiguration(CreateVoucherRequest request) {
        // Validate percentage discount
        if (DiscountType.PERCENTAGE.name().equals(request.discountType())) {
            if (request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage discount cannot exceed 100%");
            }

            if (request.hasMaxDiscount() == null) {
                throw new BadRequestException("Max discount must be specified for percentage discount");
            }

            if (request.hasMaxDiscount() && request.maxDiscountAmount() == null) {
                throw new BadRequestException("Max discount amount is required when has max discount is true for percentage discount");
            }
        }

        if (DiscountType.FIXED_AMOUNT.name().equals(request.discountType())) {
            if (request.discountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Fixed amount discount must be greater than 0");
            }

            if(request.hasMaxDiscount() != null || request.maxDiscountAmount() != null) {
                throw new BadRequestException("Has max discount or max discount amount should not be specified for fixed amount discount");
            }
        }
    }

    // Validate usage configuration
    private void validateUsageConfiguration(CreateVoucherRequest request) {
        if (request.maxUsagePerUser() > request.maxTotalUsage()) {
            throw new BadRequestException("Max usage per user cannot exceed max total usage");
        }
    }

    // Validate products for product-specific voucher
    private void validateProducts(CreateVoucherRequest request) {
        if (VoucherType.PRODUCT_SPECIFIC.name().equals(request.voucherType())) {
            if (request.productIds() == null || request.productIds().isEmpty()) {
                throw new BadRequestException("Product IDs are required for product-specific voucher");
            }

            // Check for duplicate product IDs
            Set<Long> uniqueIds = new HashSet<>(request.productIds());
            if (uniqueIds.size() != request.productIds().size()) {
                throw new BadRequestException("Duplicate product IDs found");
            }

            // Validate all products exist
            List<Long> existingProductIds = productRepository.findExistingProductIds(request.productIds());
            if (existingProductIds.size() != request.productIds().size()) {
                request.productIds().removeAll(existingProductIds);
                throw new ResourceNotFoundException("Products not found with IDs: " + request.productIds());
            }
        } else {
            if (request.productIds() != null && !request.productIds().isEmpty()) {
                throw new BadRequestException("Product IDs should not be provided for shop-wide voucher");
            }
        }
    }

    private Voucher buildVoucher(CreateVoucherRequest request, Language language) {
        Voucher voucher =  Voucher.builder()
                .type(VoucherType.valueOf(request.voucherType()))
                .code(request.code().toUpperCase())
                .startDate(LocalDate.parse(request.startDate()).atStartOfDay())
                .endDate(LocalDate.parse(request.endDate()).atTime(23, 59, 59))
                .allowPreSave(request.allowSaveBeforeActive())
                .discountType(DiscountType.valueOf(request.discountType()))
                .discountValue(request.discountValue())
                .hasMaxDiscount(request.hasMaxDiscount() != null ? request.hasMaxDiscount() : false)
                .maxDiscountAmount(request.maxDiscountAmount() != null ? request.maxDiscountAmount() : BigDecimal.ZERO)
                .minOrderAmount(request.minOrderAmount())
                .maxTotalUsage(request.maxTotalUsage())
                .maxUsagePerUser(request.maxUsagePerUser())
                .currentUsage(0)
                .active(request.isActive())
                .voucherProducts(new ArrayList<>())
                .voucherUsages(new ArrayList<>())
                .orders(new ArrayList<>())
                .translations(new ArrayList<>())
                .build();

        // Save voucher first to get ID
        voucher = voucherRepository.save(voucher);

        // Create translation for input language
        VoucherTranslation inputTranslation = VoucherTranslation.builder()
                .voucher(voucher)
                .language(language)
                .name(request.name())
                .build();
        voucher.getTranslations().add(inputTranslation);

        // Create translation for target language
        Language targetLang = (language == Language.VI) ? Language.EN : Language.VI;
        String translatedName = translationService.translate(request.name(), targetLang.name());

        VoucherTranslation targetTranslation = VoucherTranslation.builder()
                .voucher(voucher)
                .language(targetLang)
                .name(translatedName)
                .build();
        voucher.getTranslations().add(targetTranslation);

        return voucher;
    }

    private void createVoucherProducts(Voucher voucher, List<Long> productIds) {
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));

            if (!ProductStatus.ACTIVE.equals(product.getStatus())) {
                throw new BadRequestException("Product with ID " + productId + " is not active and cannot be added to this voucher");
            }

            VoucherProduct voucherProduct = VoucherProduct.builder()
                    .voucher(voucher)
                    .product(product)
                    .active(true)
                    .build();

            voucher.getVoucherProducts().add(voucherProduct);
            voucherProductRepository.save(voucherProduct);
        }
    }
}
