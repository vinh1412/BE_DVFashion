/*
 * @ {#} CreateAddressRequest.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/*
 * @description: DTO for creating a new address.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Builder
public record CreateAddressRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "Ward is required")
        String ward,

        @NotBlank(message = "Street is required")
        String street,

        @NotNull(message = "isDefault is required")
        Boolean isDefault
) {}
