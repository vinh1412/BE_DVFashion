/*
 * @ {#} PayPalInitResponse.java   1.0     12/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: Response DTO for initializing PayPal payment
 * @author: Tran Hien Vinh
 * @date:   12/10/2025
 * @version:    1.0
 */
public record PayPalInitResponse(
        String orderNumber,

        String paypalApprovalUrl
) {}
