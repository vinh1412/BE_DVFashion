/*
 * @ {#} UpdatePromotionRequest.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/*
 * @description: Request DTO for updating a promotion
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
public record UpdatePromotionRequest(
        @Size(max = 255, message = "Promotion name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Pattern(regexp = "NEW_CUSTOMER_DISCOUNT|FLASH_SALE|SEASONAL_EVENT|CLEARANCE_SALE|HOLIDAY_PROMOTION|GENERAL_DISCOUNT", message = "Invalid promotion type")
        String type,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in format yyyy-MM-dd")
        String startDate,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in format yyyy-MM-dd")
        String endDate,

        Boolean active,

        @Valid
        List<UpdatePromotionProductRequest> promotionProducts
) {}
