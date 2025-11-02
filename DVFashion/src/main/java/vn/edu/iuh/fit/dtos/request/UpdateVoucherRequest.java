/*
 * @ {#} UpdateVoucherRequest.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for Update Voucher Request
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public record UpdateVoucherRequest(
        @Pattern(regexp = "SHOP_WIDE|PRODUCT_SPECIFIC", message = "Invalid voucher type")
        String voucherType,

        @Size(max = 255, message = "Voucher name must not exceed 255 characters")
        String name,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in format yyyy-MM-dd")
        String startDate,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in format yyyy-MM-dd")
        String endDate,

        Boolean allowSaveBeforeActive,

        @Pattern(regexp = "PERCENTAGE|FIXED_AMOUNT", message = "Invalid discount type")
        String discountType,

        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        Boolean hasMaxDiscount,

        @DecimalMin(value = "0.01", message = "Max discount amount must be greater than 0")
        BigDecimal maxDiscountAmount,

        @DecimalMin(value = "0", message = "Minimum order amount must be non-negative")
        BigDecimal minOrderAmount,

        @Min(value = 1, message = "Max total usage must be at least 1")
        Integer maxTotalUsage,

        @Min(value = 1, message = "Max usage per user must be at least 1")
        Integer maxUsagePerUser,

        Boolean isActive,

        List<Long> productIds
) {}
