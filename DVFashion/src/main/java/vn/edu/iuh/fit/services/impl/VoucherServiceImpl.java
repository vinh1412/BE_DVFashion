/*
 * @ {#} VoucherServiceImpl.java   1.0     02/11/2025
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
import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
import vn.edu.iuh.fit.dtos.request.UpdateVoucherRequest;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.dtos.response.VoucherStatisticsResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.ResourceNotFoundException;
import vn.edu.iuh.fit.mappers.VoucherMapper;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.VoucherProductRepository;
import vn.edu.iuh.fit.repositories.VoucherRepository;
import vn.edu.iuh.fit.repositories.VoucherUsageRepository;
import vn.edu.iuh.fit.services.OrderItemService;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.services.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for Voucher operations
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;

    private final ProductRepository productRepository;

    private final TranslationService translationService;

    private final VoucherProductRepository voucherProductRepository;

    private final VoucherMapper voucherMapper;

    private final VoucherUsageRepository voucherUsageRepository;

    private final OrderItemService orderItemService;

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

    @Transactional
    @Override
    public VoucherResponse updateVoucher(Long id, UpdateVoucherRequest request, Language language) {
        // Find existing voucher
        Voucher existingVoucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found with ID: " + id));

        // Validate business rules
        validateVoucherForUpdate(existingVoucher, request);

        // Validate dates
        if (request.startDate() != null || request.endDate() != null) {
            validateDatesForUpdate(request.startDate(), request.endDate(), existingVoucher);
        }

        // Validate discount configuration
        validateUpdateDiscountConfiguration(request);

        // Validate usage configuration
        validateUsageConfigurationForUpdate(request, existingVoucher);

        // Validate products if voucher is product-specific
        validateProductsForUpdate(request, existingVoucher);

        // Update voucher entity
        updateVoucherEntity(existingVoucher, request, language);

        // Update voucher products if product-specific
        if (VoucherType.PRODUCT_SPECIFIC.name().equals(request.voucherType()) &&
                request.productIds() != null) {
            updateVoucherProducts(existingVoucher, request.productIds());
        }

        return voucherMapper.mapToResponse(existingVoucher, language);
    }

    @Override
    public VoucherResponse removeProductFromVoucher(Long voucherId, Long productId, Language language) {
        // Find existing voucher
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found with ID: " + voucherId));

        // Validate voucher type
        if (voucher.getType() != VoucherType.PRODUCT_SPECIFIC) {
            throw new BadRequestException("Can only remove products from PRODUCT_SPECIFIC vouchers");
        }

        // Check if voucher has been used
        if (voucher.getCurrentUsage() > 0) {
            throw new BadRequestException("Cannot remove products from voucher that has been used");
        }

        // Find the voucher product relationship
        VoucherProduct voucherProduct = voucher.getVoucherProducts().stream()
                .filter(vp -> vp.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " is not associated with this voucher"));

        // Check if this is the last product
        if (Boolean.TRUE.equals(voucherProduct.getActive())) {
            long activeProductCount = voucher.getVoucherProducts().stream()
                    .filter(VoucherProduct::getActive)
                    .count();

            if (activeProductCount <= 1) {
                throw new BadRequestException("Cannot remove the last active product from voucher");
            }
        }

        // Remove the voucher product relationship
        voucher.getVoucherProducts().remove(voucherProduct);
        voucherProductRepository.delete(voucherProduct);

        log.info("Removed product {} from voucher {}", productId, voucherId);

        return voucherMapper.mapToResponse(voucher, language);
    }

    @Transactional
    @Override
    public void deleteVoucher(Long voucherId, Language language) {
        // Find existing voucher
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found with ID: " + voucherId));

        // Validate voucher can be deleted
        validateVoucherForDeletion(voucher);

        // Delete voucher products first
        if (!voucher.getVoucherProducts().isEmpty()) {
            voucherProductRepository.deleteAll(voucher.getVoucherProducts());
        }

        // Delete voucher
        voucherRepository.delete(voucher);

        log.info("Deleted voucher with ID: {}", voucherId);
    }

    @Override
    public VoucherResponse getVoucherById(Long voucherId, Language language) {
        // Find voucher by ID
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found with ID: " + voucherId));

        return voucherMapper.mapToResponse(voucher, language);
    }

    @Override
    public List<VoucherResponse> getAllVouchersForAdmin(Language language) {
        // Get all vouchers for admin
        List<Voucher> vouchers = voucherRepository.findAll();

        // Map to response
        return vouchers.stream()
                .map(voucher -> voucherMapper.mapToResponse(voucher, language))
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherResponse> getAllAvailableVouchersForCustomer(Language language) {
        LocalDateTime now = LocalDateTime.now();

        // Get only available vouchers for customers
        List<Voucher> vouchers = voucherRepository.findAvailableVouchersForCustomers(now);

        log.info("Retrieved {} available vouchers for customers", vouchers.size());

        // Map to response
        return vouchers.stream()
                .map(voucher -> voucherMapper.mapToResponse(voucher, language))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<VoucherResponse> getVouchersForAdminPaging(Pageable pageable, Language language) {
        // Get all vouchers with pagination for admin
        Page<Voucher> voucherPage = voucherRepository.findAll(pageable);

        // Map each Voucher entity to VoucherResponse DTO
        Page<VoucherResponse> responsePage = voucherPage.map(voucher ->
                voucherMapper.mapToResponse(voucher, language));

        // Convert Page<VoucherResponse> to PageResponse<VoucherResponse>
        return PageResponse.from(responsePage);
    }

    @Override
    public PageResponse<VoucherResponse> getAvailableVouchersForCustomerPaging(Pageable pageable, Language language) {
        LocalDateTime now = LocalDateTime.now();

        // Get available vouchers with pagination for customers
        Page<Voucher> voucherPage = voucherRepository.findAvailableVouchersForCustomersPaging(now, pageable);

        // Map each Voucher entity to VoucherResponse DTO
        Page<VoucherResponse> responsePage = voucherPage.map(voucher ->
                voucherMapper.mapToResponse(voucher, language));

        // Convert Page<VoucherResponse> to PageResponse<VoucherResponse>
        return PageResponse.from(responsePage);
    }

    @Override
    public Voucher validateAndApplyVoucher(String code, User customer, Order order) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new NotFoundException("Voucher not found with code: " + code));

        log.info("Validating voucher {} for customer {}", code, customer.getEmail());

        // Check validate and time
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new BadRequestException("Voucher is not active");
        }

        if (now.isBefore(voucher.getStartDate())) {
            throw new BadRequestException("Voucher is not yet active");
        }

        if (now.isAfter(voucher.getEndDate())) {
            throw new BadRequestException("Voucher has expired");
        }

        if (voucher.getCurrentUsage() >= voucher.getMaxTotalUsage()) {
            throw new BadRequestException("Voucher usage limit reached");
        }

        // Check user usage limit
        long userUsageCount = voucher.getVoucherUsages().stream()
                .filter(u -> u.getUser().getId().equals(customer.getId()))
                .count();

        if (userUsageCount >= voucher.getMaxUsagePerUser()) {
            throw new BadRequestException("You have reached the maximum usage limit for this voucher");
        }

        // Check minimum order amount
        BigDecimal subtotal = orderItemService.calculateSubtotal(order.getItems());
        if (voucher.getMinOrderAmount() != null && subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Order amount is below minimum required for this voucher");
        }

        log.info("Order subtotal: {}, Voucher min order amount: {}", subtotal, voucher.getMinOrderAmount());

        return voucher;
    }

    @Override
    public BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotal, List<OrderItem> items) {
        BigDecimal discount = BigDecimal.ZERO;
        switch (voucher.getDiscountType()) {
            case PERCENTAGE -> {
                discount = subtotal.multiply(voucher.getDiscountValue().divide(BigDecimal.valueOf(100)));
                if (voucher.getHasMaxDiscount() && voucher.getMaxDiscountAmount() != null)
                    discount = discount.min(voucher.getMaxDiscountAmount());
            }
            case FIXED_AMOUNT -> discount = voucher.getDiscountValue();
        }

        // If voucher is PRODUCT_SPECIFIC → only applies to products in voucher
        if (voucher.getType() == VoucherType.PRODUCT_SPECIFIC) {
            Set<Long> validProductIds = voucher.getVoucherProducts().stream()
                    .filter(VoucherProduct::getActive)
                    .map(vp -> vp.getProduct().getId())
                    .collect(Collectors.toSet());
            discount = items.stream()
                    .filter(i -> validProductIds.contains(i.getProductVariant().getProduct().getId()))
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                            .multiply(voucher.getDiscountValue().divide(BigDecimal.valueOf(100))))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void recordUsage(Voucher voucher, User user, Order order) {
        voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
        VoucherUsage usage = VoucherUsage.builder()
                .voucher(voucher)
                .user(user)
                .order(order)
                .usedAt(LocalDateTime.now())
                .build();
        voucherUsageRepository.save(usage);
    }

    @Override
    public VoucherStatisticsResponse getVoucherStatistics() {
        LocalDateTime now = LocalDateTime.now();

        // Get total counts
        long totalVouchers = voucherRepository.countTotalVouchers();

        // Get counts by active status
        long totalActiveVouchers = voucherRepository.countVouchersByActiveStatus(true);

        // Get counts by inactive status
        long totalInactiveVouchers = voucherRepository.countVouchersByActiveStatus(false);

        // Get expired and currently active vouchers
        long totalExpiredVouchers = voucherRepository.countExpiredVouchers(now);

        // Get currently active vouchers
        long totalCurrentlyActiveVouchers = voucherRepository.countCurrentlyActiveVouchers(now);

        // Initialize map with all active statuses set to 0
        Map<Boolean, Long> vouchersByActiveStatus = new HashMap<>();
        vouchersByActiveStatus.put(true, 0L);
        vouchersByActiveStatus.put(false, 0L);

        // Get actual counts and override the default 0 values
        List<Object[]> statusCounts = voucherRepository.countVouchersByAllActiveStatuses();
        statusCounts.forEach(row -> {
            Boolean active = (Boolean) row[0];
            Long count = (Long) row[1];
            vouchersByActiveStatus.put(active, count);
        });

        return VoucherStatisticsResponse.builder()
                .totalVouchers(totalVouchers)
                .totalActiveVouchers(totalActiveVouchers)
                .totalInactiveVouchers(totalInactiveVouchers)
                .totalExpiredVouchers(totalExpiredVouchers)
                .totalCurrentlyActiveVouchers(totalCurrentlyActiveVouchers)
                .vouchersByActiveStatus(vouchersByActiveStatus)
                .build();
    }

    private void validateVoucherForDeletion(Voucher voucher) {
        // Check if voucher has been used
        if (voucher.getCurrentUsage() > 0) {
            throw new BadRequestException("Cannot delete voucher that has been used. Current usage: " + voucher.getCurrentUsage());
        }

        // Check if voucher is currently active
        if (Boolean.TRUE.equals(voucher.getActive())) {
            LocalDateTime now = LocalDateTime.now();

            // Check if voucher is currently in its active period
            if ((voucher.getStartDate().isBefore(now) || voucher.getStartDate().isEqual(now)) &&
                    voucher.getEndDate().isAfter(now)) {
                throw new BadRequestException("Cannot delete voucher that is currently active. Please deactivate first.");
            }
        }

        // Check if any voucher products are active
        if (voucher.getType() == VoucherType.PRODUCT_SPECIFIC &&
                !voucher.getVoucherProducts().isEmpty()) {

            boolean hasActiveProducts = voucher.getVoucherProducts().stream()
                    .anyMatch(VoucherProduct::getActive);

            if (hasActiveProducts) {
                throw new BadRequestException("Cannot delete voucher with active products. Please deactivate all products first.");
            }
        }

        // Check if voucher has any pending orders
        if (!voucher.getOrders().isEmpty()) {
            long pendingOrders = voucher.getOrders().stream()
                    .filter(order -> order.getStatus() != null &&
                            (OrderStatus.PENDING.name().equals(order.getStatus().name()) ||
                                OrderStatus.PROCESSING.name().equals(order.getStatus().name())))
                    .count();

            if (pendingOrders > 0) {
                throw new BadRequestException("Cannot delete voucher with pending or processing orders");
            }
        }

        // Check if voucher start date is in the future but too close
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate().isAfter(now)) {
            long hoursUntilStart = java.time.Duration.between(now, voucher.getStartDate()).toHours();
            if (hoursUntilStart < 24) {
                throw new BadRequestException("Cannot delete voucher that starts within 24 hours");
            }
        }

        // Check if voucher has been saved by users (if tracking user saves)
        if (voucher.getVoucherUsages() != null && !voucher.getVoucherUsages().isEmpty()) {
            boolean hasSavedVouchers = voucher.getVoucherUsages().stream()
                    .anyMatch(usage -> usage.getUsedAt() == null); // Saved but not used

            if (hasSavedVouchers) {
                throw new BadRequestException("Cannot delete voucher that has been saved by users");
            }
        }
    }

    // Validation voucher for update
    private void validateVoucherForUpdate(Voucher existingVoucher, UpdateVoucherRequest request) {
        // Cannot update voucher that has been used
        if (existingVoucher.getCurrentUsage() > 0) {
            // Only allow certain fields to be updated if voucher has been used
            if (request.voucherType() != null && !existingVoucher.getType().name().equals(request.voucherType())) {
                throw new BadRequestException("Cannot change voucher type for a voucher that has been used");
            }

            if (request.discountType() != null && !existingVoucher.getDiscountType().name().equals(request.discountType())) {
                throw new BadRequestException("Cannot change discount type for a voucher that has been used");
            }

            if (request.discountValue() != null && existingVoucher.getDiscountValue().compareTo(request.discountValue()) != 0) {
                throw new BadRequestException("Cannot change discount value for a voucher that has been used");
            }

            if (request.minOrderAmount() != null && existingVoucher.getMinOrderAmount().compareTo(request.minOrderAmount()) != 0) {
                throw new BadRequestException("Cannot change minimum order amount for a voucher that has been used");
            }
        }

        // Cannot change voucher type if it has been used
        if (existingVoucher.getCurrentUsage() > 0 &&
                !existingVoucher.getType().name().equals(request.voucherType())) {
            throw new BadRequestException("Cannot change voucher type for voucher that has been used");
        }
    }

    // Validate dates for update
    private void validateDatesForUpdate(String startDateStr, String endDateStr, Voucher existingVoucher) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = (startDateStr != null) ? LocalDate.parse(startDateStr) : existingVoucher.getStartDate().toLocalDate();
        LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : existingVoucher.getEndDate().toLocalDate();

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        if (endDate.isBefore(today)) {
            throw new BadRequestException("End date cannot be in the past");
        }

        // Cannot change start date if voucher has already started and been used
        if (existingVoucher.getCurrentUsage() > 0 &&
                existingVoucher.getStartDate().toLocalDate().isBefore(today) &&
                !existingVoucher.getStartDate().toLocalDate().equals(startDate)) {
            throw new BadRequestException("Cannot change start date for voucher that has already started and been used");
        }

        // Cannot set end date before current date if voucher is currently active
        if (existingVoucher.getStartDate().toLocalDate().isBefore(today) &&
                existingVoucher.getEndDate().toLocalDate().isAfter(today) &&
                endDate.isBefore(today)) {
            throw new BadRequestException("Cannot set end date in the past for currently active voucher");
        }
    }

    // Validate usage configuration for update
    private void validateUsageConfigurationForUpdate(UpdateVoucherRequest request, Voucher existingVoucher) {
        Integer maxTotal = (request.maxTotalUsage() != null) ? request.maxTotalUsage() : existingVoucher.getMaxTotalUsage();
        Integer maxPerUser = (request.maxUsagePerUser() != null) ? request.maxUsagePerUser() : existingVoucher.getMaxUsagePerUser();

        if (maxPerUser > maxTotal) {
            throw new BadRequestException("Max usage per user cannot exceed max total usage");
        }

        // Cannot reduce max total usage below current usage
        if (request.maxTotalUsage() != null && request.maxTotalUsage() < existingVoucher.getCurrentUsage()) {
            throw new BadRequestException("Cannot set max total usage below current usage: " + existingVoucher.getCurrentUsage());
        }

        // Validate max usage per user against existing usage data
        if (existingVoucher.getCurrentUsage() > 0  && request.maxUsagePerUser() != null) {
            Integer maxCurrentUserUsage = voucherRepository.findMaxUsagePerUser(existingVoucher.getId());
            if (maxCurrentUserUsage != null && request.maxUsagePerUser() < maxCurrentUserUsage) {
                throw new BadRequestException("Cannot set max usage per user below current maximum user usage: " + maxCurrentUserUsage);
            }
        }
    }

    // Validate products for update
    private void validateProductsForUpdate(UpdateVoucherRequest request, Voucher existingVoucher) {
        VoucherType newType = (request.voucherType() != null)
                ? VoucherType.valueOf(request.voucherType())
                : existingVoucher.getType();

        if (existingVoucher.getType() == VoucherType.PRODUCT_SPECIFIC &&
                newType == VoucherType.SHOP_WIDE) {
            voucherProductRepository.deleteAll(existingVoucher.getVoucherProducts());
            existingVoucher.getVoucherProducts().clear();
        }

        if (newType == VoucherType.PRODUCT_SPECIFIC) {
            if (request.productIds() != null && !request.productIds().isEmpty()) {

                // Check for duplicate product IDs
                Set<Long> uniqueIds = new HashSet<>(request.productIds());
                if (uniqueIds.size() != request.productIds().size()) {
                    throw new BadRequestException("Duplicate product IDs found");
                }

                // Validate all products exist and are active
                List<Product> products = productRepository.findAllById(request.productIds());
                if (products.size() != request.productIds().size()) {
                    List<Long> foundIds = products.stream().map(Product::getId).toList();
                    List<Long> notFoundIds = request.productIds().stream()
                            .filter(id -> !foundIds.contains(id))
                            .toList();
                    throw new ResourceNotFoundException("Products not found with IDs: " + notFoundIds);
                }

                // Check if all products are active
                List<Long> inactiveProductIds = products.stream()
                        .filter(product -> !ProductStatus.ACTIVE.equals(product.getStatus()))
                        .map(Product::getId)
                        .toList();

                if (!inactiveProductIds.isEmpty()) {
                    throw new BadRequestException("Inactive products cannot be added to voucher. Product IDs: " + inactiveProductIds);
                }
            }
        } else {
            if (request.productIds() != null && !request.productIds().isEmpty()) {
                throw new BadRequestException("Product IDs should not be provided for shop-wide voucher");
            }
        }
    }

    // Update voucher entity with request data
    private void updateVoucherEntity(Voucher voucher, UpdateVoucherRequest request, Language language) {
        if (request.voucherType() != null) {
            voucher.setType(VoucherType.valueOf(request.voucherType()));
        }

        if (request.startDate() != null) {
            voucher.setStartDate(LocalDate.parse(request.startDate()).atStartOfDay());
        }

        if (request.endDate() != null) {
            voucher.setEndDate(LocalDate.parse(request.endDate()).atTime(23, 59, 59));
        }

        if (request.allowSaveBeforeActive() != null) {
            voucher.setAllowPreSave(request.allowSaveBeforeActive());
        }

        if (request.discountType() != null) {
            voucher.setDiscountType(DiscountType.valueOf(request.discountType()));
        }

        if (request.discountValue() != null) {
            voucher.setDiscountValue(request.discountValue());
        }

        if (request.hasMaxDiscount() != null) {
            voucher.setHasMaxDiscount(request.hasMaxDiscount());
        }

        if (request.maxDiscountAmount() != null) {
            voucher.setMaxDiscountAmount(request.maxDiscountAmount());
        }

        if (request.minOrderAmount() != null) {
            voucher.setMinOrderAmount(request.minOrderAmount());
        }

        if (request.maxTotalUsage() != null) {
            voucher.setMaxTotalUsage(request.maxTotalUsage());
        }

        if (request.maxUsagePerUser() != null) {
            voucher.setMaxUsagePerUser(request.maxUsagePerUser());
        }

        if (request.isActive() != null) {
            voucher.setActive(request.isActive());

            if (!request.isActive()) {
                if (voucher.getVoucherProducts() != null && !voucher.getVoucherProducts().isEmpty()) {
                    voucher.getVoucherProducts().forEach(vp -> vp.setActive(false));
                }
            }
        }

        // Update translation for input language
        if (request.name() != null) {
            updateVoucherTranslation(voucher, language, request.name());

            // Update translation for target language
            Language targetLang = (language == Language.VI) ? Language.EN : Language.VI;
            String translatedName = translationService.translate(request.name(), targetLang.name());
            updateVoucherTranslation(voucher, targetLang, translatedName);
        }
    }

    // Update or create voucher translation
    private void updateVoucherTranslation(Voucher voucher, Language language, String name) {
        VoucherTranslation translation = voucher.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElse(null);

        if (translation != null) {
            translation.setName(name);
        } else {
            VoucherTranslation newTranslation = VoucherTranslation.builder()
                    .voucher(voucher)
                    .language(language)
                    .name(name)
                    .build();
            voucher.getTranslations().add(newTranslation);
        }
    }

    private void updateVoucherProducts(Voucher voucher, List<Long> newProductIds) {
        // Lấy danh sách productId hiện tại
        Set<Long> currentProductIds = voucher.getVoucherProducts().stream()
                .map(vp -> vp.getProduct().getId())
                .collect(Collectors.toSet());

        // Xóa các sản phẩm không còn trong newProductIds
        List<VoucherProduct> toRemove = voucher.getVoucherProducts().stream()
                .filter(vp -> !newProductIds.contains(vp.getProduct().getId()))
                .collect(Collectors.toList());
        toRemove.forEach(vp -> {
            voucher.getVoucherProducts().remove(vp);
            voucherProductRepository.delete(vp);
        });

        // Thêm mới các sản phẩm chưa có
        for (Long pid : newProductIds) {
            if (!currentProductIds.contains(pid)) {
                Product product = productRepository.findById(pid)
                        .orElseThrow(() -> new NotFoundException("Product not found: " + pid));
                VoucherProduct vp = new VoucherProduct();
                vp.setVoucher(voucher);
                vp.setProduct(product);
                vp.setActive(true);
                voucher.getVoucherProducts().add(vp);
            }
        }
    }

    // Update voucher products
//    private void updateVoucherProducts(Voucher voucher, List<Long> newProductIds) {
//        // Get current product IDs
//        Set<Long> currentProductIds = voucher.getVoucherProducts().stream()
//                .map(vp -> vp.getProduct().getId())
//                .collect(Collectors.toSet());
//
//        Set<Long> newProductIdSet = new HashSet<>(newProductIds);
//
//        // Find products to remove (in current but not in new)
//        Set<Long> productsToRemove = currentProductIds.stream()
//                .filter(id -> !newProductIdSet.contains(id))
//                .collect(Collectors.toSet());
//
//        // Find products to add (in new but not in current)
//        Set<Long> productsToAdd = newProductIdSet.stream()
//                .filter(id -> !currentProductIds.contains(id))
//                .collect(Collectors.toSet());
//
//        // Remove products
//        voucher.getVoucherProducts().removeIf(vp -> productsToRemove.contains(vp.getProduct().getId()));
//
//        // Add new products
//        for (Long productId : productsToAdd) {
//            Product product = productRepository.findById(productId)
//                    .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
//
//            VoucherProduct voucherProduct = VoucherProduct.builder()
//                    .voucher(voucher)
//                    .product(product)
//                    .active(true)
//                    .build();
//
//            voucher.getVoucherProducts().add(voucherProduct);
//            voucherProductRepository.save(voucherProduct);
//        }
//    }

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

    private void validateUpdateDiscountConfiguration(UpdateVoucherRequest request){
        if (request.discountType() == null && request.discountValue() == null) {
            return;
        }

        DiscountType type = (request.discountType() != null)
                ? DiscountType.valueOf(request.discountType())
                : null;

        if (type == DiscountType.PERCENTAGE ||
                (type == null && DiscountType.PERCENTAGE.name().equalsIgnoreCase(request.discountType()))) {

            if (request.discountValue() != null && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage discount cannot exceed 100%");
            }

            // Nếu đang cập nhật maxDiscount thì phải có flag hasMaxDiscount
            if (request.hasMaxDiscount() != null && request.hasMaxDiscount() && request.maxDiscountAmount() == null) {
                throw new BadRequestException("Max discount amount is required when hasMaxDiscount = true");
            }

            // Nếu có flag hasMaxDiscount = false mà vẫn truyền maxDiscountAmount
            if (request.hasMaxDiscount() != null && !request.hasMaxDiscount() && request.maxDiscountAmount() != null) {
                throw new BadRequestException("Max discount amount should not be provided when hasMaxDiscount = false");
            }
        }

        if (type == DiscountType.FIXED_AMOUNT ||
                (type == null && DiscountType.FIXED_AMOUNT.name().equalsIgnoreCase(request.discountType()))) {

            if (request.discountValue() != null && request.discountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Fixed amount discount must be greater than 0");
            }

            if (Boolean.TRUE.equals(request.hasMaxDiscount()) || request.maxDiscountAmount() != null) {
                throw new BadRequestException("hasMaxDiscount or maxDiscountAmount should not be specified for fixed amount discount");
            }
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
