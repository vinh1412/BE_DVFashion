/*
 * @ {#} BrevoEmailServiceImpl.java   1.0     03/12/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.response.OrderItemResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.dtos.response.PaymentResponse;
import vn.edu.iuh.fit.dtos.response.ShippingInfoResponse;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.services.BrevoEmailService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @description: Implementation of email service using Brevo API
 * @author: Tran Hien Vinh
 * @date:   03/12/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class BrevoEmailServiceImpl implements BrevoEmailService {
    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name}")
    private String senderName;

    @Value("${brevo.brevo-base-url}")
    private String BREVO_URL;

    private final RestTemplate restTemplate;

    @Override
    public void sendTemplateEmail(String toEmail, int templateId, Map<String, Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();


        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("sender", Map.of(
                "email", senderEmail,
                "name", senderName
        ));
        body.put("templateId", templateId);
        body.put("params", params);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(BREVO_URL, request, String.class);
    }

    @Override
    public void sendOrderConfirmationEmail(OrderResponse orderResponse, String customerEmail) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerName", orderResponse.customerName());
        params.put("orderNumber", orderResponse.orderNumber());
        params.put("orderDate", orderResponse.orderDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("orderStatus", getStatusDisplayName(orderResponse.status()));
        params.put("paymentMethod", getPaymentMethodDisplayName(orderResponse.payment()));

        params.put("shippingName", orderResponse.shippingInfo().fullName());
        params.put("shippingPhone", orderResponse.shippingInfo().phone());
        params.put("shippingAddress", buildShippingAddress(orderResponse.shippingInfo()));

        params.put("subTotal", formatCurrency(orderResponse.subtotal()));
        params.put("shippingFee", formatCurrency(orderResponse.shippingFee()));
        params.put("discountAmount", orderResponse.discountAmount() != null ?
                formatCurrency(orderResponse.discountAmount()) : "0");
        params.put("totalAmount", formatCurrency(orderResponse.totalAmount()));

        // HTML for order items
        params.put("itemsHtml", buildOrderItemsHtml(orderResponse.items()));

        int templateId = 1; // Template ID của Brevo

        this.sendTemplateEmail(customerEmail, templateId, params);
    }

    // Helper methods
    private String buildOrderItemsHtml(List<OrderItemResponse> items) {
        StringBuilder html = new StringBuilder();

        for (OrderItemResponse item : items) {

            html.append(
                    "<div style=\"padding: 0 20px;\">" +
                            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" " +
                            "style=\"margin-bottom: 25px; border-collapse: collapse;\">" +

                            // ROW
                            "  <tr>" +

                            // IMAGE – RESPONSIVE
                            "    <td width=\"90\" valign=\"top\" " +
                            "        style=\"padding-bottom:10px; display:block; width:100%; max-width:90px;\">" +
                            "      <img src=\"" + (item.imageUrl() == null ? "https://via.placeholder.com/80" : item.imageUrl()) + "\" " +
                            "           style=\"width:80px; height:80px; object-fit:cover; border-radius:6px; border:1px solid #ccc;\"" +
                            "      />" +
                            "    </td>" +

                            // PRODUCT INFO – RESPONSIVE BLOCK
                            "    <td valign=\"top\" " +
                            "        style=\"padding-left:15px; padding-bottom:10px; display:block; width:100%;\">" +
                            "      <p style=\"margin:0; font-weight:600; font-size:18px; color:#000;\">" + item.productName() + "</p>" +
                            "      <p style=\"margin:3px 0; color:#555;\">Màu: " + item.color() + "</p>" +
                            "      <p style=\"margin:3px 0; color:#555;\">Size: " + item.sizeName() + "</p>" +
                            "    </td>" +

                            // PRICE – RESPONSIVE BLOCK
                            "    <td valign=\"top\" align=\"right\" " +
                            "        style=\"display:block; width:100%; text-align:right;\">" +
                            "      <p style=\"margin:0; font-size:16px;\">x" + item.quantity() + "</p>" +
                            "      <p style=\"margin:5px 0 0 0; font-size:20px; font-weight:600; color:#000;\">" +
                            formatCurrency(item.totalPrice()) + " VND</p>" +
                            "    </td>" +

                            "  </tr>" +
                            "</table>" +
                            "</div>"
            );
        }

        return html.toString();
    }


    private String buildShippingAddress(ShippingInfoResponse shipping) {
        return String.format("%s", shipping.fullAddress());
    }

    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case PROCESSING -> "Đang xử lý";
            case SHIPPED -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELED -> "Đã hủy";
            case RETURNED -> "Đã trả hàng";
        };
    }

    private String getStatusColor(OrderStatus status) {
        return switch (status) {
            case PENDING -> "#ffc107";
            case CONFIRMED -> "#007bff";
            case PROCESSING -> "#17a2b8";
            case SHIPPED -> "#fd7e14";
            case DELIVERED -> "#28a745";
            case CANCELED -> "#dc3545";
            case RETURNED -> "#6c757d";
        };
    }

    private String getStatusUpdateMessage(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Đơn hàng của bạn đã được xác nhận và sẽ được xử lý sớm nhất.";
            case PROCESSING -> "Đơn hàng đang được chuẩn bị và đóng gói.";
            case SHIPPED ->
                    "Đơn hàng đã được giao cho đơn vị vận chuyển. Bạn sẽ sớm nhận được hàng.";
            case DELIVERED -> "Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng!";
            case CANCELED -> "Đơn hàng đã bị hủy. Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.";
            case RETURNED -> "Đơn hàng đã được trả lại. Chúng tôi sẽ xử lý hoàn tiền sớm nhất.";
            default -> "Trạng thái đơn hàng đã được cập nhật.";
        };
    }

    private String getPaymentMethodDisplayName(PaymentResponse payment) {
        if (payment == null) return "Chưa xác định";
        return switch (payment.paymentMethod()) {
            case CASH_ON_DELIVERY -> "Thanh toán khi nhận hàng";
            case PAYPAL -> "PayPal";
            default -> payment.paymentMethod().toString();
        };
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}
