/*
 * @ {#} PaymentService.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.Payment;
import vn.edu.iuh.fit.enums.PaymentMethod;

/*
 * @description: Service interface for handling payment processing
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
public interface PaymentService {
    /**
     * Create a payment record for the given order and payment method.
     *
     * @param order         the order to create a payment for
     * @param paymentMethod the method of payment
     * @return the created payment entity
     */
    Payment createPayment(Order order, PaymentMethod paymentMethod);
}
