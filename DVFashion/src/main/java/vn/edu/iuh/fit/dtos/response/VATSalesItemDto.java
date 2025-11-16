/*
 * @ {#} VATSalesItemDto.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @description: Response DTO for tax reports
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VATSalesItemDto(
        int stt,

        String productName,

        String unit,

        int quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

        BigDecimal vatRate,

        BigDecimal vatAmount,

        String orderNumber,

        LocalDate orderDate,

        String buyerName,

        String buyerTaxCode
) {}