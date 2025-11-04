/*
 * @ {#} VoucherProductResponse.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for Voucher Product Response
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public record VoucherProductResponse(
        Long id,

        Long productId,

        String productName,

        Boolean active
) {}
