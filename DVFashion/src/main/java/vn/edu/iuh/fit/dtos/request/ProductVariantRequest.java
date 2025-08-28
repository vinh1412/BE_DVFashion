/*
 * @ {#} ProductVariantRequest.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Request DTO for creating or updating a product variant
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductVariantRequest(
        @NotBlank(message = "Color is required")
        String color,

        @DecimalMin(value = "0.0", message = "Additional price must be >= 0")
        BigDecimal additionalPrice,

        @NotBlank(message = "Status is required")
        String status, // ProductVariantStatus

        @NotEmpty(message = "At least one size is required")
        List<@Valid SizeRequest> sizes,

        List<@Valid ProductVariantImageRequest> images
) {}
