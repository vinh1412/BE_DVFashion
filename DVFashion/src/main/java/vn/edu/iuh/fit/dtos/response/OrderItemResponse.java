/*
 * @ {#} OrderItemResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;

/*
 * @description: Response DTO for an item in an order
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record OrderItemResponse(
         Long productVariantId,

         String productName,

         String color,

         String sizeName,

         int quantity,

         BigDecimal unitPrice,

         BigDecimal discount,

         BigDecimal totalPrice,

         String imageUrl
) {}
