/*
 * @ {#} TaxReportServiceImpl.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.TaxReportResponse;
import vn.edu.iuh.fit.dtos.response.VATSalesListReport;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.OrderItem;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.TaxReportService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @description: Service implementation for VAT tax report operations
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaxReportServiceImpl implements TaxReportService {
    private final OrderRepository orderRepository;
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(10); // 10% VAT

    @Override
    public TaxReportResponse getVATTaxReport(LocalDate startDate, LocalDate endDate) {
        // Get delivered orders within the date range
        List<Order> orders = orderRepository.findOrdersForReport(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                List.of(OrderStatus.DELIVERED)
        );

        // Build VAT sales list
        VATSalesListReport vatSalesList = buildVATSalesList(orders, startDate, endDate);

        // Calculate totals
        BigDecimal totalRevenue = vatSalesList.summary().totalAmountBeforeVAT();
        BigDecimal totalVAT = vatSalesList.summary().totalVATAmount();
        BigDecimal totalAmountIncludingVAT = vatSalesList.summary().totalAmountIncludingVAT();

        long totalOrders = orders.size();
        long totalItems = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .count();

        return TaxReportResponse.builder()
                .reportTitle("BÁO CÁO THUẾ VAT")
                .reportPeriod(formatPeriod(startDate, endDate))
                .fromDate(startDate)
                .toDate(endDate)
                .generatedDate(LocalDate.now())
                .totalRevenue(totalRevenue)
                .totalVAT(totalVAT)
                .totalAmountIncludingVAT(totalAmountIncludingVAT)
                .totalOrders(totalOrders)
                .totalItems(totalItems)
                .vatSalesList(vatSalesList)
                .build();
    }

    @Override
    public byte[] generateVATTaxReportExcel(LocalDate startDate, LocalDate endDate) {
        TaxReportResponse report = getVATTaxReport(startDate, endDate);
        return createVATExcelReport(report);
    }

    private VATSalesListReport buildVATSalesList(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        List<VATSalesListReport.VATSalesItem> items = new ArrayList<>();
        int stt = 1;

        BigDecimal totalAmountBeforeVAT = BigDecimal.ZERO;
        BigDecimal totalVATAmount = BigDecimal.ZERO;

        for (Order order : orders) {
            for (OrderItem orderItem : order.getItems()) {
                // Get product name from product variant
                String productName = orderItem.getProductVariant().getProduct().getTranslations().stream()
                        .filter(t -> t.getLanguage() == vn.edu.iuh.fit.enums.Language.VI)
                        .findFirst()
                        .map(t -> t.getName())
                        .orElse("Sản phẩm");

                // Add color and size info to product name
                String color = orderItem.getProductVariant().getColor();
                String sizeName = orderItem.getSize().getSizeName();
                productName = productName + " - " + color + " - " + sizeName;

                BigDecimal quantity = BigDecimal.valueOf(orderItem.getQuantity());
                
                // Calculate unit price after discount
                BigDecimal unitPrice = orderItem.getUnitPrice();
                if (orderItem.getDiscount() != null && orderItem.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    unitPrice = unitPrice.subtract(orderItem.getDiscount());
                }

                // Total amount before VAT
                BigDecimal totalAmount = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

                // Calculate VAT amount
                BigDecimal vatAmount = totalAmount.multiply(VAT_RATE)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                VATSalesListReport.VATSalesItem item = VATSalesListReport.VATSalesItem.builder()
                        .stt(stt++)
                        .productName(productName)
                        .unit("Cái")
                        .quantity(quantity)
                        .unitPrice(unitPrice)
                        .totalAmount(totalAmount)
                        .vatRate(VAT_RATE)
                        .vatAmount(vatAmount)
                        .build();

                items.add(item);

                totalAmountBeforeVAT = totalAmountBeforeVAT.add(totalAmount);
                totalVATAmount = totalVATAmount.add(vatAmount);
            }
        }

        BigDecimal totalAmountIncludingVAT = totalAmountBeforeVAT.add(totalVATAmount);

        VATSalesListReport.VATSummary summary = VATSalesListReport.VATSummary.builder()
                .totalAmountBeforeVAT(totalAmountBeforeVAT)
                .totalVATAmount(totalVATAmount)
                .totalAmountIncludingVAT(totalAmountIncludingVAT)
                .totalItems(items.size())
                .build();

        return VATSalesListReport.builder()
                .reportTitle("BẢNG KÊ BÁN RA (01-1/GTGT)")
                .taxPeriod(formatTaxPeriod(startDate, endDate))
                .companyName("DVFASHION SHOP")
                .taxCode("0123456789")
                .address("12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP. Hồ Chí Minh")
                .generatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .items(items)
                .summary(summary)
                .build();
    }

    private byte[] createVATExcelReport(TaxReportResponse report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Bảng kê bán ra");

            // Set up print settings
            sheet.setAutobreaks(true);
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

            // Create styles
            Map<String, CellStyle> styles = createExcelStyles(workbook);

            int rowNum = 0;

            // Create header
            rowNum = createVATHeader(sheet, styles, report.vatSalesList(), rowNum);

            // Create data table
            rowNum = createVATDataTable(sheet, styles, report.vatSalesList(), rowNum);

            // Create footer
            createVATFooter(sheet, styles, report.vatSalesList(), rowNum);

            // Auto size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating VAT tax report Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate VAT tax report Excel", e);
        }
    }

    private Map<String, CellStyle> createExcelStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        // Title style
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("title", titleStyle);

        // Company style
        CellStyle companyStyle = workbook.createCellStyle();
        Font companyFont = workbook.createFont();
        companyFont.setBold(true);
        companyFont.setFontHeightInPoints((short) 12);
        companyStyle.setFont(companyFont);
        styles.put("company", companyStyle);

        // Normal style
        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setAlignment(HorizontalAlignment.LEFT);
        styles.put("normal", normalStyle);

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(true);
        styles.put("header", headerStyle);

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("data", dataStyle);

        // Data center style
        CellStyle dataCenterStyle = workbook.createCellStyle();
        dataCenterStyle.cloneStyleFrom(dataStyle);
        dataCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        styles.put("dataCenter", dataCenterStyle);

        // Currency style
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        styles.put("currency", currencyStyle);

        // Total style
        CellStyle totalStyle = workbook.createCellStyle();
        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        totalStyle.setBorderTop(BorderStyle.THIN);
        totalStyle.setBorderBottom(BorderStyle.THIN);
        totalStyle.setBorderLeft(BorderStyle.THIN);
        totalStyle.setBorderRight(BorderStyle.THIN);
        totalStyle.setAlignment(HorizontalAlignment.CENTER);
        totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("total", totalStyle);

        // Total currency style
        CellStyle totalCurrencyStyle = workbook.createCellStyle();
        totalCurrencyStyle.cloneStyleFrom(totalStyle);
        totalCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalCurrencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        styles.put("totalCurrency", totalCurrencyStyle);

        return styles;
    }

    private int createVATHeader(XSSFSheet sheet, Map<String, CellStyle> styles, 
                                VATSalesListReport vatReport, int rowNum) {
        // Company name
        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue("Đơn vị: " + vatReport.companyName());
        companyCell.setCellStyle(styles.get("company"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        // Report form
        Cell formCell = companyRow.createCell(4);
        formCell.setCellValue("Mẫu số: 01-1/GTGT");
        formCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));

        // Tax code
        Row taxCodeRow = sheet.createRow(rowNum++);
        Cell taxCodeCell = taxCodeRow.createCell(0);
        taxCodeCell.setCellValue("Mã số thuế: " + vatReport.taxCode());
        taxCodeCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        // Reference
        Cell refCell = taxCodeRow.createCell(4);
        refCell.setCellValue("(Ban hành theo Thông tư số 78/2014/TT-BTC");
        refCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));

        // Address
        Row addressRow = sheet.createRow(rowNum++);
        Cell addressCell = addressRow.createCell(0);
        addressCell.setCellValue("Địa chỉ: " + vatReport.address());
        addressCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        // Reference continued
        Cell refCell2 = addressRow.createCell(4);
        refCell2.setCellValue("ngày 18/6/2014 của Bộ Tài chính)");
        refCell2.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));

        // Empty row
        sheet.createRow(rowNum++);

        // Report title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(vatReport.reportTitle());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        titleRow.setHeightInPoints(25);

        // Tax period
        Row periodRow = sheet.createRow(rowNum++);
        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue(vatReport.taxPeriod());
        periodCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    private int createVATDataTable(XSSFSheet sheet, Map<String, CellStyle> styles, 
                                   VATSalesListReport vatReport, int rowNum) {
        // Table header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(40);

        String[] headers = {
                "STT",
                "Tên hàng hóa, dịch vụ",
                "Đơn vị tính",
                "Số lượng",
                "Đơn giá\n(chưa có thuế GTGT)",
                "Thành tiền\n(chưa có thuế GTGT)",
                "Thuế suất\nGTGT (%)",
                "Tiền thuế\nGTGT"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }

        // Data rows
        for (VATSalesListReport.VATSalesItem item : vatReport.items()) {
            Row row = sheet.createRow(rowNum++);

            // STT
            Cell sttCell = row.createCell(0);
            sttCell.setCellValue(item.stt());
            sttCell.setCellStyle(styles.get("dataCenter"));

            // Product name
            Cell nameCell = row.createCell(1);
            nameCell.setCellValue(item.productName());
            nameCell.setCellStyle(styles.get("data"));

            // Unit
            Cell unitCell = row.createCell(2);
            unitCell.setCellValue(item.unit());
            unitCell.setCellStyle(styles.get("dataCenter"));

            // Quantity
            Cell quantityCell = row.createCell(3);
            quantityCell.setCellValue(item.quantity().doubleValue());
            quantityCell.setCellStyle(styles.get("currency"));

            // Unit price
            Cell unitPriceCell = row.createCell(4);
            unitPriceCell.setCellValue(item.unitPrice().doubleValue());
            unitPriceCell.setCellStyle(styles.get("currency"));

            // Total amount
            Cell totalAmountCell = row.createCell(5);
            totalAmountCell.setCellValue(item.totalAmount().doubleValue());
            totalAmountCell.setCellStyle(styles.get("currency"));

            // VAT rate
            Cell vatRateCell = row.createCell(6);
            vatRateCell.setCellValue(item.vatRate().doubleValue());
            vatRateCell.setCellStyle(styles.get("dataCenter"));

            // VAT amount
            Cell vatAmountCell = row.createCell(7);
            vatAmountCell.setCellValue(item.vatAmount().doubleValue());
            vatAmountCell.setCellStyle(styles.get("currency"));
        }

        // Total row
        Row totalRow = sheet.createRow(rowNum++);
        
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("TỔNG CỘNG");
        totalLabelCell.setCellStyle(styles.get("total"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        Cell totalAmountCell = totalRow.createCell(5);
        totalAmountCell.setCellValue(vatReport.summary().totalAmountBeforeVAT().doubleValue());
        totalAmountCell.setCellStyle(styles.get("totalCurrency"));

        Cell emptyCell = totalRow.createCell(6);
        emptyCell.setCellStyle(styles.get("total"));

        Cell totalVATCell = totalRow.createCell(7);
        totalVATCell.setCellValue(vatReport.summary().totalVATAmount().doubleValue());
        totalVATCell.setCellStyle(styles.get("totalCurrency"));

        return rowNum;
    }

    private void createVATFooter(XSSFSheet sheet, Map<String, CellStyle> styles, 
                                 VATSalesListReport vatReport, int rowNum) {
        // Empty row
        rowNum++;

        // Footer info
        Row footerRow = sheet.createRow(rowNum++);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("Tổng số tiền thanh toán (bao gồm thuế GTGT): " + 
                formatCurrency(vatReport.summary().totalAmountIncludingVAT()));
        footerCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Empty row
        rowNum++;

        // Signature date
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(5);
        dateCell.setCellValue("Ngày " + LocalDate.now().getDayOfMonth() + " tháng " + 
                LocalDate.now().getMonthValue() + " năm " + LocalDate.now().getYear());
        dateCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 5, 7));

        // Signature positions
        Row signatureRow = sheet.createRow(rowNum++);
        
        Cell accountantCell = signatureRow.createCell(1);
        accountantCell.setCellValue("Người lập biểu");
        accountantCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 2));

        Cell chiefAccountantCell = signatureRow.createCell(3);
        chiefAccountantCell.setCellValue("Kế toán trưởng");
        chiefAccountantCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 3, 4));

        Cell directorCell = signatureRow.createCell(6);
        directorCell.setCellValue("Giám đốc");
        directorCell.setCellStyle(styles.get("normal"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 6, 7));

        // Empty rows for signatures
        for (int i = 0; i < 3; i++) {
            sheet.createRow(rowNum++);
        }
    }

    private String formatPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Từ %s đến %s", startDate.format(formatter), endDate.format(formatter));
    }

    private String formatTaxPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Kỳ tính thuế: %s - %s", startDate.format(formatter), endDate.format(formatter));
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f đ", amount);
    }
}
