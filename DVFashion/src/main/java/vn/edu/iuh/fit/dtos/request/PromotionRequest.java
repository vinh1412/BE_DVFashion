/*
 * @ {#} PromotionRequest.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.math.BigDecimal;

/*
 * @description: Request DTO for creating or updating a promotion
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public record PromotionRequest(
        @NotBlank(message = "Promotion name must not be blank", groups = ValidationGroups.Create.class)
        String name,

        String description,

        @NotBlank(message = "Promotion type is required", groups = ValidationGroups.Create.class)
        String type,

        @DecimalMin(value = "0.0", message = "Value must be >= 0", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        BigDecimal value,

        @DecimalMin(value = "0.0", message = "Minimum order amount must be >= 0", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        BigDecimal minOrderAmount,

        @DecimalMin(value = "0.0", message = "Maximum discount amount must be >= 0", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Integer maxUsages,

        @NotBlank(message = "Start date is required", groups = ValidationGroups.Create.class)
        @Pattern(
                regexp = "\\d{4}-\\d{2}-\\d{2}",
                message = "Start date must be in format yyyy-MM-dd",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
        )
        String startDate,

        @NotBlank(message = "End date is required", groups = ValidationGroups.Create.class)
        @Pattern(
                regexp = "\\d{4}-\\d{2}-\\d{2}",
                message = "End date must be in format yyyy-MM-dd",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
        )
        String endDate,

        Boolean active
) {}
