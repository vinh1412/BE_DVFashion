/*
 * @ {#} BrevoEmailService.java   1.0     30/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.OrderResponse;
import java.util.Map;

/*
 * @description: Service interface for handling email-related operations using Brevo API
 * @author: Tran Hien Vinh
 * @date:   30/11/2025
 * @version:    1.0
 */

public interface BrevoEmailService {
    /**
     * Sends a templated email using Brevo API.
     *
     * @param toEmail    the recipient's email address
     * @param templateId the ID of the email template to use
     * @param params     a map of parameters to populate the template
     */
    void sendTemplateEmail(String toEmail, int templateId, Map<String, Object> params);

    /**
     * Send order confirmation email to customer
     *
     * @param orderResponse the order details
     * @param customerEmail the customer's email address
     */
    void sendOrderConfirmationEmail(OrderResponse orderResponse, String customerEmail);
}
