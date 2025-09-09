/*
 * @ {#} CartItemResponse.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description: DTO for individual cart item response
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public record CartItemResponse(
        Long cartItemId,

        String productName,

        String color,

        String sizeName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

        String imageUrl,

        LocalDateTime reservedUntil
) {}
