/*
 * @ {#} AdminUpdateOrderRequest.java   1.0     12/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/*
 * @description: Request DTO for admin updating an order
 * @author: Tran Hien Vinh
 * @date:   12/10/2025
 * @version:    1.0
 */
@Builder
public record AdminUpdateOrderRequest(
        String fullName,

        String phone,

        String country,

        String city,

        String district,

        String ward,

        String street,

        @Size(max = 500, message = "notes must be <= 500 characters")
        String notes,

        @Pattern(regexp = "PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED|RETURNED", message = "Invalid order status")
        String orderStatus,

        @Pattern(regexp = "PENDING|COMPLETED|FAILED|REFUNDED|CANCELED", message = "Invalid payment status")
        String paymentStatus
) {}
