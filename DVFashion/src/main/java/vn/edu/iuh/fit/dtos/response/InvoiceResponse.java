/*
 * @ {#} InvoiceResponse.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO for invoice response
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Builder
public record InvoiceResponse(
        String invoiceNumber,
        String orderNumber,
        LocalDateTime invoiceDate,
        LocalDateTime orderDate,
        String customerName,
        String customerEmail,
        String customerPhone,
        ShippingInfoResponse shippingInfo,
        List<InvoiceItemResponse> items,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal voucherDiscount,
        String voucherCode,
        BigDecimal total,
        String paymentMethod,
        String paymentStatus,
        String orderStatus
) {
    @Builder
    public record InvoiceItemResponse(
            String productName,
            String variantColor,
            String size,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}

    @Builder
    public record ShippingInfoResponse(
            String fullName,
            String phone,
            String address
    ) {}
}
