/*
 * @ {#} CartResponse.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: DTO for shopping cart response
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public record CartResponse(
    Long cartId,

    Integer totalItems,

    BigDecimal totalAmount,

    List<CartItemResponse> items
){}
