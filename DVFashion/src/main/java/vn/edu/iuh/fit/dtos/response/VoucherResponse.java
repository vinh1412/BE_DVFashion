/*
 * @ {#} VoucherResponse.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.DiscountType;
import vn.edu.iuh.fit.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO for Voucher Response
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public record VoucherResponse(
        Long id,

        VoucherType voucherType,

        String name,

        String code,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Boolean allowSaveBeforeActive,

        DiscountType discountType,

        BigDecimal discountValue,

        Boolean hasMaxDiscount,

        BigDecimal maxDiscountAmount,

        BigDecimal minOrderAmount,

        Integer maxTotalUsage,

        Integer maxUsagePerUser,

        Integer currentUsage,

        Boolean active,

        List<VoucherProductResponse> products
) {}
