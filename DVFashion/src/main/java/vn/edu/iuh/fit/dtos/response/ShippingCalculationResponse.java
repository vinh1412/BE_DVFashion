/*
 * @ {#} ShippingCalculationResponse.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description: DTO for shipping calculation response
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public record ShippingCalculationResponse(
        BigDecimal shippingFee,

        LocalDateTime estimatedDeliveryTime,

        String deliveryTimeText
) {}
