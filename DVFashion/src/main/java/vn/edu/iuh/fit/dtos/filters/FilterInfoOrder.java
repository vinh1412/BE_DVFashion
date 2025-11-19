/*
 * @ {#} FilterInfoOrder.java   1.0     19/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.dtos.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @description: Filter criteria for querying orders
 * @author: Tran Hien Vinh
 * @date:   19/11/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfoOrder {

    // Search keyword
    private String search;

    // Order status filter
    private OrderStatus status;

    // Payment filters
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    // Customer filter
    private Long customerId;

    // Total price range filter
    private BigDecimal minTotal;
    private BigDecimal maxTotal;

    // Date range filter
    private LocalDate startDate;
    private LocalDate endDate;
}
