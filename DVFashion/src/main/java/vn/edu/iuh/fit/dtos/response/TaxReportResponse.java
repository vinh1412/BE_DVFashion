/*
 * @ {#} TaxReportResponse.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @description: DTO for tax report response
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Builder
public record TaxReportResponse(
        String reportTitle,
        String reportPeriod,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDate generatedDate,
        BigDecimal totalRevenue,
        BigDecimal totalVAT,
        BigDecimal totalAmountIncludingVAT,
        long totalOrders,
        long totalItems,
        VATSalesListReport vatSalesList
) {
}
