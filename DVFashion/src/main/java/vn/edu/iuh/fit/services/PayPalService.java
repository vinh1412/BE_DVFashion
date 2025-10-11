/*
 * @ {#} PayPalService1.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import java.math.BigDecimal;

/*
 * @description: Service interface for PayPal payment processing
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
public interface PayPalService {
    /**
     * Create a “PayPal Order” (on the PayPal system) corresponding to the order in the system.
     *
     * @param total       the total amount for the payment
     * @param orderNumber the unique order number associated with the payment
     * @return the approval URL for the PayPal payment
     */
    String createPayment(BigDecimal total, String orderNumber);

    /**
     * Capture a PayPal payment for the given order ID (PayPal transaction ID) after the user approves the payment.
     *
     * @param orderId the unique order ID associated with the payment
     */
    void capturePayment(String orderId);
}
