/*
 * @ {#} SizeRequest.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.iuh.fit.validators.ValidationGroups;

/*
 * @description: Request DTO for size information
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record SizeRequest(
        @NotBlank(message = "Size name is required", groups = ValidationGroups.Create.class)
        String sizeName,

        @NotNull(message = "Stock quantity is required", groups = ValidationGroups.Create.class)
        @Min(value = 0, message = "Stock quantity must be >= 0", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Integer stockQuantity
) {}
