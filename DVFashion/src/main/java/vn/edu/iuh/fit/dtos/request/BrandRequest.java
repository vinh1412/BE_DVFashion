/*
 * @ {#} CategoryRequest.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: Request DTO for creating or updating a brand
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */

import jakarta.validation.constraints.NotBlank;
import vn.edu.iuh.fit.validators.ValidationGroups;

public record BrandRequest(
        @NotBlank(message = "Name is required", groups = ValidationGroups.Create.class)
        String name,

        String description,

        String logo,

        Boolean active
) {
}
