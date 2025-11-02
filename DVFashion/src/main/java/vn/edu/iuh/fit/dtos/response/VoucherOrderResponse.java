/*
 * @ {#} VoucherOrderResponse.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.DiscountType;

import java.math.BigDecimal;

/*
 * @description: Response DTO for voucher details in an order
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public record VoucherOrderResponse(
        Long id,

        String code,

        DiscountType discountType,

        BigDecimal discountAmount
) {}
