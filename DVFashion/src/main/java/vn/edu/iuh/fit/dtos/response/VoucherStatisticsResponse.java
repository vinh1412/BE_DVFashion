/*
 * @ {#} VoucherStatisticsResponse.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.util.Map;

/*
 * @description: DTO for voucher statistics response
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Builder
public record VoucherStatisticsResponse(
        long totalVouchers,

        long totalActiveVouchers,

        long totalInactiveVouchers,

        long totalExpiredVouchers,

        long totalCurrentlyActiveVouchers,

        Map<Boolean, Long> vouchersByActiveStatus
) {
}
