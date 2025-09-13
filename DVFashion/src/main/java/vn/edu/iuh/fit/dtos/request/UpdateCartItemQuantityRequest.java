/*
 * @ {#} UpdateCartItemQuantityRequest.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import javax.validation.constraints.Min;

/*
 * @description: Request DTO for updating the quantity of a cart item
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
public record UpdateCartItemQuantityRequest(
        @Min(value = 1, message = "Quantity must be zero or greater")
        Integer newQuantity
) {}
