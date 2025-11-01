/*
 * @ {#} PromotionRequest.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description: Request DTO for creating or updating a promotion
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public record CreatePromotionRequest(
        @NotBlank(message = "Promotion name must not be blank")
        String name,

        String description,

        @Pattern(regexp = "NEW_CUSTOMER_DISCOUNT|FLASH_SALE|SEASONAL_EVENT|CLEARANCE_SALE|HOLIDAY_PROMOTION|GENERAL_DISCOUNT", message = "Invalid promotion type")
        String type,

        @NotBlank(message = "Start date is required")
        @Pattern(
                regexp = "\\d{4}-\\d{2}-\\d{2}",
                message = "Start date must be in format yyyy-MM-dd"
        )
        String startDate,

        @NotBlank(message = "End date is required")
        @Pattern(
                regexp = "\\d{4}-\\d{2}-\\d{2}",
                message = "End date must be in format yyyy-MM-dd"
        )
        String endDate,

        Boolean active,

        @NotEmpty(message = "Promotion must have at least one product")
        List<PromotionProductRequest> promotionProducts
) {}
