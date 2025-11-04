/*
 * @ {#} PromotionOrderResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;

/*
 * @description: Response DTO for promotion details in an order
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record PromotionOrderResponse(
        Long id,

        String name,

        BigDecimal discountValue,

        String discountType
) {}
