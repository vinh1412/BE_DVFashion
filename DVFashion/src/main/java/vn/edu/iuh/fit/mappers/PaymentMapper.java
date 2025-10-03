/*
 * @ {#} PaymentMapper.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.PaymentResponse;
import vn.edu.iuh.fit.entities.Payment;

/*
 * @description: Mapper class for converting Payment entities to PaymentResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Component
public class PaymentMapper {
    public PaymentResponse mapPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentDate()
        );
    }
}
