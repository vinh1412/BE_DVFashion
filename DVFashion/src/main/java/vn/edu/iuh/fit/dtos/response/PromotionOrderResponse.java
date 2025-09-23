/*
 * @ {#} PromotionOrderResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record PromotionOrderResponse(
        Long id,
        String code,
        String name,
        BigDecimal discountValue,
        String discountType
) {
}
