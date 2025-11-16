/*
 * @ {#} InvoiceController.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.InvoiceResponse;
import vn.edu.iuh.fit.services.InvoiceService;

/*
 * @description: REST controller for managing invoice-related operations
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/invoices")
@RequiredArgsConstructor
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @PathVariable String orderNumber) {
        InvoiceResponse invoice = invoiceService.getInvoiceByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice retrieved successfully"));
    }

    @GetMapping("/{orderNumber}/download")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String orderNumber) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(orderNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-" + orderNumber + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/{orderNumber}/preview")
    public ResponseEntity<byte[]> previewInvoicePdf(@PathVariable String orderNumber) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(orderNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("invoice-" + orderNumber + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
