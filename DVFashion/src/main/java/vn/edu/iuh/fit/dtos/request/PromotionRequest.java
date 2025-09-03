/*
 * @ {#} PromotionRequest.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/*
 * @description: Request DTO for creating or updating a promotion
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public record PromotionRequest(
        @NotBlank(message = "Promotion name must not be blank")
        String name,

        String description,

        @NotBlank(message = "Promotion type is required")
        String type,

        @DecimalMin(value = "0.0", message = "Value must be >= 0")
        BigDecimal value,

        @DecimalMin(value = "0.0", message = "Minimum order amount must be >= 0")
        BigDecimal minOrderAmount,

        @DecimalMin(value = "0.0", message = "Maximum discount amount must be >= 0")
        Integer maxUsages,

        @NotBlank(message = "Start date is required")
        String startDate,

        @NotBlank(message = "End date is required")
        String endDate,

        Boolean active
) {}
