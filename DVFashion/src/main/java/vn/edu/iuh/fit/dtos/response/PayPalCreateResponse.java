/*
 * @ {#} PayPalCreateResponse.java   1.0     13/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: Response DTO for PayPal payment creation
 * @author: Tran Hien Vinh
 * @date:   13/10/2025
 * @version:    1.0
 */
public record PayPalCreateResponse(
        String paypalPaymentId,

        String approvalUrl
) {}
