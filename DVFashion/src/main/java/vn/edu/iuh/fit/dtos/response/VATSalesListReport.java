/*
 * @ {#} VATSalesListReport.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: Response DTO for tax reports
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VATSalesListReport(
        String reportTitle,

        String reportPeriod,

        LocalDate fromDate,

        LocalDate toDate,

        LocalDate generatedDate,

        List<VATSalesItemDto> items,

        BigDecimal totalAmount,

        BigDecimal totalVATAmount,
        
        BigDecimal totalIncludingVAT
) {}
