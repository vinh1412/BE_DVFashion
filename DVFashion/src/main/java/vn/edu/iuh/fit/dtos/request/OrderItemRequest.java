/*
 * @ {#} OrderItemRequest.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotNull;

/*
 * @description: DTO for an item in an order request
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record OrderItemRequest(
       @NotNull(message = "Cart item ID is required")
       Long cartItemId
) {}
