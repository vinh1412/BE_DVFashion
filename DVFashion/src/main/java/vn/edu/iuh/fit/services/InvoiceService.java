/*
 * @ {#} InvoiceService.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.InvoiceResponse;

/*
 * @description: Service interface for invoice-related operations
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
public interface InvoiceService {
    /**
     * Generates a PDF for the invoice associated with the given order number.
     *
     * @param orderNumber The order number for which to generate the invoice PDF.
     * @return A byte array representing the generated PDF.
     */
    byte[] generateInvoicePdf(String orderNumber);

    /**
     * Retrieves the invoice details for the given order number.
     *
     * @param orderNumber The order number for which to retrieve the invoice.
     * @return An InvoiceResponse containing the invoice details.
     */
    InvoiceResponse getInvoiceByOrderNumber(String orderNumber);
}
