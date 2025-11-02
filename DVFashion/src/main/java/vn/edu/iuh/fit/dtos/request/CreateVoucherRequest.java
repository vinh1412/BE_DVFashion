/*
 * @ {#} CreateVoucherRequest.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for Create Voucher Request
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public record CreateVoucherRequest(
        @NotBlank(message = "Voucher type is required")
        @Pattern(regexp = "SHOP_WIDE|PRODUCT_SPECIFIC", message = "Voucher type must be either SHOP_WIDE or PRODUCT_SPECIFIC")
        String voucherType,

        @NotBlank(message = "Voucher name is required")
        @Size(min = 3, max = 100, message = "Voucher name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Voucher code is required")
        @Pattern(regexp = "^[A-Z0-9_-]{3,20}$", message = "Voucher code must be 3-20 characters, uppercase letters, numbers, underscore or dash only")
        String code,

        @NotNull(message = "Start date is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must be in format YYYY-MM-DD")
        String startDate,

        @NotNull(message = "End date is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must be in format YYYY-MM-DD")
        String endDate,

        @NotNull(message = "Allow save before active is required")
        Boolean allowSaveBeforeActive,

        @NotBlank(message = "Discount type is required")
        @Pattern(regexp = "PERCENTAGE|FIXED_AMOUNT", message = "Discount type must be either PERCENTAGE or FIXED_AMOUNT")
        String discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        Boolean hasMaxDiscount,

        @DecimalMin(value = "0.01", message = "Max discount amount must be greater than 0")
        BigDecimal maxDiscountAmount,

        @NotNull(message = "Minimum order amount is required")
        @DecimalMin(value = "0", message = "Minimum order amount must be non-negative")
        BigDecimal minOrderAmount,

        @NotNull(message = "Maximum total usage is required")
        @Min(value = 1, message = "Maximum total usage must be at least 1")
        Integer maxTotalUsage,

        @NotNull(message = "Maximum usage per user is required")
        @Min(value = 1, message = "Maximum usage per user must be at least 1")
        Integer maxUsagePerUser,

        @NotNull(message = "Active status is required")
        Boolean isActive,

        List<@NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be positive") Long> productIds
) {}
