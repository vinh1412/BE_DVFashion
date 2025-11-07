/*
 * @ {#} FilterInfoProduct.java   1.0     07/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @description: Filter information for querying products
 * @author: Tran Hien Vinh
 * @date:   07/11/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfoProduct {
    private String search;

    private Long categoryId;

    private Long promotionId;

    private ProductStatus status;

    private Boolean onSale;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private LocalDate startDate;

    private LocalDate endDate;
}
