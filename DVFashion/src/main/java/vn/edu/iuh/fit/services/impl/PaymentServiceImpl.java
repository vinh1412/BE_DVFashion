/*
 * @ {#} PaymentServiceImpl.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.Payment;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;
import vn.edu.iuh.fit.services.PaymentService;
import vn.edu.iuh.fit.utils.OrderUtils;

import java.math.BigDecimal;

/*
 * @description: Implementation of PaymentService for handling payment processing
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Override
    public Payment createPayment(Order order, PaymentMethod paymentMethod) {
        BigDecimal totalAmount = calculateOrderTotal(order);

        return Payment.builder()
                .transactionId(OrderUtils.generateTransactionId())
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .order(order)
                .build();
    }

    private BigDecimal calculateOrderTotal(Order order) {
        BigDecimal itemsTotal = order.getItems().stream()
                .map(item -> item.getUnitPrice().subtract(item.getDiscount())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return itemsTotal.add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
    }
}
