/*
 * @ {#} InvoiceServiceImpl.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.InvoiceResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.OrderException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.InvoiceService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FontUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * @description: Implementation of InvoiceService for generating PDF invoices
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {
    private final OrderRepository orderRepository;

    private final UserService userService;

    @Override
    public byte[] generateInvoicePdf(String orderNumber) {
        InvoiceResponse invoice = getInvoiceByOrderNumber(orderNumber);
        return createPdfInvoice(invoice);
    }

    @Override
    public InvoiceResponse getInvoiceByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Check permission
        UserResponse currentUser = userService.getCurrentUser();
        if (currentUser.getRoles().contains("ROLE_CUSTOMER") &&
                !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        // Only allow invoice generation for confirmed, processing, shipped, or delivered orders
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELED) {
            throw new OrderException("Invoice not available for order status: " + order.getStatus());
        }

        return mapToInvoiceResponse(order);
    }

    // Map Order entity to InvoiceResponse DTO
    private InvoiceResponse mapToInvoiceResponse(Order order) {
        List<InvoiceResponse.InvoiceItemResponse> items = order.getItems().stream()
                .map(item -> InvoiceResponse.InvoiceItemResponse.builder()
                        .productName(item.getProductVariant().getProduct().getTranslations().get(0).getName())
                        .variantColor(item.getProductVariant().getColor())
                        .size(item.getSize().getSizeName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal
                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .subtract(order.getVoucherDiscount() != null ? order.getVoucherDiscount() : BigDecimal.ZERO);

        return InvoiceResponse.builder()
                .invoiceNumber("INV-" + order.getOrderNumber())
                .orderNumber(order.getOrderNumber())
                .invoiceDate(LocalDateTime.now())
                .orderDate(order.getOrderDate())
                .customerName(order.getCustomer().getFullName())
                .customerEmail(order.getCustomer().getEmail())
                .customerPhone(order.getCustomer().getPhone())
                .shippingInfo(InvoiceResponse.ShippingInfoResponse.builder()
                        .fullName(order.getShippingInfo().getFullName())
                        .phone(order.getShippingInfo().getPhone())
                        .address(String.format("%s, %s, %s, %s, %s",
                                order.getShippingInfo().getStreet(),
                                order.getShippingInfo().getWard(),
                                order.getShippingInfo().getDistrict(),
                                order.getShippingInfo().getCity(),
                                order.getShippingInfo().getCountry()))
                        .build())
                .items(items)
                .subtotal(subtotal)
                .shippingFee(order.getShippingFee())
                .voucherDiscount(order.getVoucherDiscount())
                .voucherCode(order.getVoucherCode())
                .total(total)
                .paymentMethod(order.getPayment().getPaymentMethod().name())
                .paymentStatus(order.getPayment().getPaymentStatus().name())
                .orderStatus(order.getStatus().name())
                .build();
    }

    // Generate PDF invoice using iText
    private byte[] createPdfInvoice(InvoiceResponse invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A5);
            document.setMargins(10, 15, 10, 15);

            // Add content to PDF
            addInvoiceHeader(document, invoice);
            addCustomerInfo(document, invoice);
            addInvoiceItems(document, invoice);
            addInvoiceSummary(document, invoice);
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF invoice: {}", e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    // Add invoice header section
    private void addInvoiceHeader(Document document, InvoiceResponse invoice) {
        PdfFont font = FontUtils.getVietnameseFont();
        PdfFont fontBold = FontUtils.getVietnameseFontBold();
        // Company info
        Paragraph companyName = new Paragraph("DVFASHION SHOP")
                .setFont(fontBold)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(companyName);

        Paragraph companyInfo = new Paragraph("Address: 12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP. Hồ Chí Minh\nPhone: 0123456789\nEmail: dvfashion@gmail.com")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(companyInfo);

        document.add(new Paragraph("\n"));

        // Invoice title
        Paragraph invoiceTitle = new Paragraph("HÓA ĐƠN / INVOICE")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(invoiceTitle);

        document.add(new Paragraph("\n"));

        // Invoice details
        Table headerTable = new Table(1);
        headerTable.setWidth(UnitValue.createPercentValue(100));

        headerTable.addCell(new Cell()
                .add(new Paragraph("Số hóa đơn: " + invoice.invoiceNumber()).setFont(font))
                .setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell()
                .add(new Paragraph("Số đơn hàng: " + invoice.orderNumber()).setFont(font))
                .setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell()
                .add(new Paragraph("Ngày xuất: " + invoice.invoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setFont(font))
                .setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell()
                .add(new Paragraph("Ngày đặt: " + invoice.orderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setFont(font))
                .setBorder(Border.NO_BORDER));

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    // Add customer information section
    private void addCustomerInfo(Document document, InvoiceResponse invoice) {
        PdfFont font = FontUtils.getVietnameseFont();
        PdfFont fontBold = FontUtils.getVietnameseFontBold();

        Paragraph customerTitle = new Paragraph("THÔNG TIN KHÁCH HÀNG")
                .setFont(fontBold)
                .setFontSize(14);
        document.add(customerTitle);

        Table customerTable = new Table(2);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        customerTable.addCell(new Cell().add(new Paragraph("Tên khách hàng: " + invoice.customerName()).setFont(font)).setBorder(Border.NO_BORDER));
        customerTable.addCell(new Cell().add(new Paragraph("Email: " + invoice.customerEmail()).setFont(font)).setBorder(Border.NO_BORDER));
        customerTable.addCell(new Cell().add(new Paragraph("Số điện thoại: " + invoice.customerPhone()).setFont(font)).setBorder(Border.NO_BORDER));
        customerTable.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        Cell addressCell = new Cell(1, 2).add(new Paragraph("Địa chỉ giao hàng: " + invoice.shippingInfo().address()).setFont(font)).setBorder(Border.NO_BORDER);
        customerTable.addCell(addressCell);

        document.add(customerTable);
        document.add(new Paragraph("\n"));
    }

    // Add invoice items section
    private void addInvoiceItems(Document document, InvoiceResponse invoice) {
        PdfFont font = FontUtils.getVietnameseFont();
        PdfFont fontBold = FontUtils.getVietnameseFontBold();

        Paragraph itemsTitle = new Paragraph("CHI TIẾT ĐƠN HÀNG")
                .setFont(fontBold)
                .setFontSize(14);
        document.add(itemsTitle);

        Table itemsTable = new Table(new float[]{3, 2, 1, 1, 2, 2});
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Headers
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Sản phẩm").setFont(fontBold)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Màu sắc").setFont(fontBold)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Size").setFont(fontBold)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("SL").setFont(fontBold)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Đơn giá").setFont(fontBold)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Thành tiền").setFont(fontBold)));

        // Items
        for (InvoiceResponse.InvoiceItemResponse item : invoice.items()) {
            itemsTable.addCell(new Cell().add(new Paragraph(item.productName()).setFont(font)));
            itemsTable.addCell(new Cell().add(new Paragraph(item.variantColor()).setFont(font)));
            itemsTable.addCell(new Cell().add(new Paragraph(item.size()).setFont(font)));
            itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.quantity())).setFont(font)));
            itemsTable.addCell(new Cell().add(new Paragraph(formatCurrency(item.unitPrice())).setFont(font)));
            itemsTable.addCell(new Cell().add(new Paragraph(formatCurrency(item.totalPrice())).setFont(font)));
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n"));
    }

    // Add invoice summary section
    private void addInvoiceSummary(Document document, InvoiceResponse invoice) {
        PdfFont font = FontUtils.getVietnameseFont();
        PdfFont fontBold = FontUtils.getVietnameseFontBold();

        Table summaryTable = new Table(2);
        summaryTable.setWidth(UnitValue.createPercentValue(60));
        summaryTable.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        summaryTable.addCell(new Cell().add(new Paragraph("Tạm tính:").setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        summaryTable.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.subtotal())).setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        summaryTable.addCell(new Cell().add(new Paragraph("Phí vận chuyển:").setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        summaryTable.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.shippingFee() != null ? invoice.shippingFee() : BigDecimal.ZERO)).setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        if (invoice.voucherDiscount() != null && invoice.voucherDiscount().compareTo(BigDecimal.ZERO) > 0) {
            summaryTable.addCell(new Cell().add(new Paragraph("Giảm giá (" + invoice.voucherCode() + "):").setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            summaryTable.addCell(new Cell().add(new Paragraph("-" + formatCurrency(invoice.voucherDiscount())).setFont(font)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        }

        summaryTable.addCell(new Cell().add(new Paragraph("TỔNG CỘNG:").setFont(fontBold)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        summaryTable.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.total())).setFont(fontBold)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // Payment info
        Paragraph paymentInfo = new Paragraph("Phương thức thanh toán: " + getPaymentMethodText(invoice.paymentMethod()))
                .setFont(font)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(paymentInfo);

        Paragraph paymentStatus = new Paragraph("Trạng thái thanh toán: " + getPaymentStatusText(invoice.paymentStatus()))
                .setFont(font)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(paymentStatus);
    }

    // Add footer section
    private void addFooter(Document document) {
        PdfFont font = FontUtils.getVietnameseFont();

        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Cảm ơn quý khách đã mua sắm tại cửa hàng!\nThank you for shopping with us!")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(footer);
    }

    private String formatCurrency(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + "đ";
    }

    private String getPaymentMethodText(String paymentMethod) {
        return switch (paymentMethod) {
            case "CASH_ON_DELIVERY" -> "Thanh toán khi nhận hàng";
            case "PAYPAL" -> "PayPal";
            default -> paymentMethod;
        };
    }

    private String getPaymentStatusText(String paymentStatus) {
        return switch (paymentStatus) {
            case "PENDING" -> "Chờ thanh toán";
            case "COMPLETED" -> "Đã hoàn thành";
            case "CANCELED" -> "Đã hủy";
            case "FAILED" -> "Thanh toán thất bại";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> paymentStatus;
        };
    }
}
