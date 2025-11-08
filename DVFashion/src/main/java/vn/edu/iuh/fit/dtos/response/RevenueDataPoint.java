/*
 * @ {#} RevenueDataPoint.java   1.0     05/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;

/*
 * @description: DTO representing a revenue data point for statistical reporting.
 * @author: Tran Hien Vinh
 * @date:   05/11/2025
 * @version:    1.0
 */
public record RevenueDataPoint(
        String period,

        BigDecimal revenue
) {}
