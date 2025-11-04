/*
 * @ {#} CancelOrderRequest.java   1.0     04/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * @description: Request DTO for cancelling an order
 * @author: Tran Hien Vinh
 * @date:   04/11/2025
 * @version:    1.0
 */
public record CancelOrderRequest(
        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String cancellationReason
) {}
