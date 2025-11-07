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
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Request DTO for creating or updating a product
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductRequest(
        @NotBlank(message = "Product name is required", groups = {ValidationGroups.Create.class})
        String name,

        String description,

        @NotNull(message = "Price is required", groups = ValidationGroups.Create.class)
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        BigDecimal price,

        @NotBlank(message = "Material is required")
        String material,

        @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        BigDecimal salePrice,

        Boolean onSale,

        @NotBlank(message = "Status is required", groups = ValidationGroups.Create.class)
        String status,

        @NotNull(message = "Category ID is required", groups = ValidationGroups.Create.class)
        Long categoryId,

//        Long promotionId,

        @NotEmpty(message = "Product must have at least one variant", groups = ValidationGroups.Create.class)
        List<@Valid ProductVariantRequest> variants
) {}