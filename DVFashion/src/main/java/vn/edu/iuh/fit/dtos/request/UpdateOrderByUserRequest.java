/*
 * @ {#} UpdateOrderByUserRequest.java   1.0     12/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/*
 * @description: Request DTO for user updating an order
 * @author: Tran Hien Vinh
 * @date:   12/10/2025
 * @version:    1.0
 */
@Builder
public record UpdateOrderByUserRequest(
        String fullName,

        String phone,

        String country,

        String city,

        String district,

        String ward,

        String street,

        @Size(max = 500, message = "notes must be <= 500 characters")
        String notes // optional
) {}
