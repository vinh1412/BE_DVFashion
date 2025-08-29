/*
 * @ {#} ProductVariantImageRequest.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import vn.edu.iuh.fit.validators.ValidationGroups;

/*
 * @description: Request DTO for ProductVariantImage
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record ProductVariantImageRequest(
        @NotNull(message = "isPrimary must not be null", groups = {ValidationGroups.Create.class})
        Boolean isPrimary,

        @NotNull(message = "Sort order must not be null", groups = {ValidationGroups.Create.class})
        @Min(value = 0, message = "Sort order must be >= 0", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Integer sortOrder
) {}
