/*
 * @ {#} ShippingInfoRequest.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record ShippingInfoRequest(
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

        @NotBlank(message = "Street address is required")
        String street
) {
}
