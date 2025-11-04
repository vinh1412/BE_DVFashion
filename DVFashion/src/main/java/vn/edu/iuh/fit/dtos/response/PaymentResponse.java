/*
 * @ {#} PaymentResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description: Response DTO for payment details
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record PaymentResponse(
         Long id,

         String transactionId,

         BigDecimal amount,

         PaymentMethod paymentMethod,

         PaymentStatus paymentStatus,

         LocalDateTime paymentDate
) {}
