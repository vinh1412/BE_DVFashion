/*
 * @ {#} VATSalesListReport.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for Vietnamese VAT Sales List Report (01-1/GTGT format)
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Builder
public record VATSalesListReport(
        String reportTitle,
        String taxPeriod,
        String companyName,
        String taxCode,
        String address,
        String generatedDate,
        List<VATSalesItem> items,
        VATSummary summary
) {
    @Builder
    public record VATSalesItem(
            int stt,
            String productName,
            String unit,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            BigDecimal vatRate,
            BigDecimal vatAmount
    ) {}

    @Builder
    public record VATSummary(
            BigDecimal totalAmountBeforeVAT,
            BigDecimal totalVATAmount,
            BigDecimal totalAmountIncludingVAT,
            int totalItems
    ) {}
}
