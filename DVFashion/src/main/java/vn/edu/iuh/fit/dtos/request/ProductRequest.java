/*
 * @ {#} ProductRequest.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Request DTO for creating or updating a product
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @NotBlank(message = "Material is required")
        String material,

        @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
        BigDecimal salePrice,

        boolean onSale,

        @NotBlank(message = "Status is required")
        String status,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Brand ID is required")
        Long brandId,

        Long promotionId,

        @NotEmpty(message = "Product must have at least one variant")
        List<@Valid ProductVariantRequest> variants
) {}