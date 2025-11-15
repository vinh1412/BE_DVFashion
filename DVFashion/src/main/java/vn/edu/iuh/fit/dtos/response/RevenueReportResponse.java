/*
 * @ {#} RevenueReportResponse.java   1.0     15/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: DTO for revenue report response
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
@Builder
public record RevenueReportResponse(
        String reportTitle,
        String reportPeriod,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDate generatedDate,
        BigDecimal totalRevenue,
        BigDecimal totalOrders,
        BigDecimal averageOrderValue,
        List<RevenueDetailResponse> details,
        RevenueComparisonResponse comparison
) {
    @Builder
    public record RevenueDetailResponse(
            String period,
            LocalDate date,
            long totalOrders,
            BigDecimal totalRevenue,
            BigDecimal totalProducts,
            BigDecimal averageOrderValue,
            BigDecimal growthRate
    ) {}

    @Builder
    public record RevenueComparisonResponse(
            BigDecimal previousPeriodRevenue,
            BigDecimal growthAmount,
            BigDecimal growthPercentage,
            String growthStatus
    ) {}
}
