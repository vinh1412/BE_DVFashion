/*
 * @ {#} TaxReportService1Impl.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import vn.edu.iuh.fit.dtos.response.TaxReportResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.services.TaxReportService;

import java.time.LocalDate;

/*
 * @description: Implementation of TaxReportService for generating tax reports
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.VATSalesItemDto;
import vn.edu.iuh.fit.dtos.response.VATSalesListReport;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.repositories.OrderRepository;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxReportServiceImpl implements TaxReportService {

    private final OrderRepository orderRepository;

    // HỆ SỐ THUẾ
    private static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.10"); // 10%

    // HỆ SỐ TRỰC TIẾP THEO NHÓM HÀNG HÓA, DỊCH VỤ
    private static final BigDecimal DIRECT_RATE_GROUP1 = new BigDecimal("0.01"); // 1%
    private static final BigDecimal DIRECT_RATE_GROUP2 = new BigDecimal("0.05"); // 5%
    private static final BigDecimal DIRECT_RATE_GROUP3 = new BigDecimal("0.03"); // 3%
    private static final BigDecimal DIRECT_RATE_GROUP4 = new BigDecimal("0.02"); // 2%

    // VAT nhóm theo mẫu 01-1/GTGT
    private enum VatGroup {
        NON_TAX, // Không chịu thuế
        VAT_0, // 0%
        VAT_5, // 5%
        VAT_10 // 10%
    }

    @Override
    public TaxReportResponse getVATSalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating VAT sales report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<OrderStatus> statuses = List.of(OrderStatus.DELIVERED);

        // Step 1: Fetch orders with customer and items (avoiding MultipleBagFetchException)
        List<Order> deliveredOrders = orderRepository.findOrdersWithItemsForTaxReport(start, end, statuses);

        if (deliveredOrders.isEmpty()) {
            log.info("No delivered orders found for the period");
            return createEmptyReport(startDate, endDate);
        }

        log.info("Found {} delivered orders for tax report", deliveredOrders.size());

        // Step 2: Eagerly fetch product variants with products and translations
        orderRepository.fetchProductVariantsWithTranslations(deliveredOrders);

        // Step 3: Eagerly fetch sizes
        orderRepository.fetchSizesForOrders(deliveredOrders);

        log.info("Completed eager loading of related entities");

        // Tạo danh sách VATSalesItemDto từ đơn hàng
        List<VATSalesItemDto> vatSalesItems = createVATSalesItems(deliveredOrders);

        // Tính tổng song song để tăng hiệu suất
        BigDecimal totalAmount = vatSalesItems.parallelStream()
                .map(VATSalesItemDto::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVAT = vatSalesItems.parallelStream()
                .map(VATSalesItemDto::vatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncludingVAT = totalAmount.add(totalVAT);

        VATSalesListReport vatSalesListReport = VATSalesListReport.builder()
                .reportTitle("BẢNG KÊ HÓA ĐƠN, CHỨNG TỪ HÀNG HÓA, DỊCH VỤ BÁN RA")
                .reportPeriod(formatPeriod(startDate, endDate))
                .fromDate(startDate)
                .toDate(endDate)
                .generatedDate(LocalDate.now())
                .items(vatSalesItems)
                .totalAmount(totalAmount)
                .totalVATAmount(totalVAT)
                .totalIncludingVAT(totalIncludingVAT)
                .build();

        return TaxReportResponse.builder()
                .vatSalesListReport(vatSalesListReport)
                .build();
    }

    private TaxReportResponse createEmptyReport(LocalDate startDate, LocalDate endDate) {
        VATSalesListReport vatSalesListReport = VATSalesListReport.builder()
                .reportTitle("BẢNG KÊ HÓA ĐƠN, CHỨNG TỪ HÀNG HÓA, DỊCH VỤ BÁN RA")
                .reportPeriod(formatPeriod(startDate, endDate))
                .fromDate(startDate)
                .toDate(endDate)
                .generatedDate(LocalDate.now())
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .totalVATAmount(BigDecimal.ZERO)
                .totalIncludingVAT(BigDecimal.ZERO)
                .build();

        return TaxReportResponse.builder()
                .vatSalesListReport(vatSalesListReport)
                .build();
    }

    @Override
    public byte[] generateVATForm011Excel(LocalDate startDate, LocalDate endDate) {
        TaxReportResponse report = getVATSalesReport(startDate, endDate);
        return createVATSalesExcelReport(report.vatSalesListReport());
    }

    @Override
    public byte[] generateVATForm04Excel(LocalDate startDate, LocalDate endDate) {
        TaxReportResponse response = getVATSalesReport(startDate, endDate);
        VATSalesListReport vatReport = response.vatSalesListReport();

        return createVATForm04ExcelReport(startDate, endDate, vatReport);
    }

    @Override
    public byte[] generateVATForm014AExcel(LocalDate startDate, LocalDate endDate) {
        TaxReportResponse vatReport = getVATSalesReport(startDate, endDate);

        return createVATForm04AExcelReport(startDate, endDate, vatReport.vatSalesListReport());
    }

    // Hàm tạo danh sách VATSalesItemDto từ đơn hàng
    private List<VATSalesItemDto> createVATSalesItems(List<Order> orders) {
        // Pre-calculate total size để tránh resize ArrayList
        int estimatedSize = orders.stream()
                .mapToInt(o -> o.getItems() != null ? o.getItems().size() : 0)
                .sum();

        List<VATSalesItemDto> items = new ArrayList<>(estimatedSize);
        AtomicInteger stt = new AtomicInteger(1);

        for (Order order : orders) {
            // Cache customer info để tránh gọi lại nhiều lần
            String buyerName = order.getCustomer() != null ?
                    order.getCustomer().getFullName() : "";
            String orderNumber = order.getOrderNumber();
            LocalDate orderDate = order.getOrderDate().toLocalDate();

            for (OrderItem orderItem : order.getItems()) {
                try {
                    // Giả sử tất cả đều áp dụng VAT 10%
                    BigDecimal vatRate = DEFAULT_VAT_RATE;

                    BigDecimal unitPrice = orderItem.getUnitPrice()
                            .subtract(orderItem.getDiscount() != null ?
                                    orderItem.getDiscount() : BigDecimal.ZERO);

                    BigDecimal totalPrice = unitPrice.multiply(
                            BigDecimal.valueOf(orderItem.getQuantity()));
                    BigDecimal vatAmount = totalPrice.multiply(vatRate)
                            .setScale(2, RoundingMode.HALF_UP);

                    VATSalesItemDto item = VATSalesItemDto.builder()
                            .stt(stt.getAndIncrement())
                            .productName(getProductName(orderItem))
                            .buyerName(buyerName)
                            .buyerTaxCode(randomTaxCode())
                            .unit("Cái")
                            .quantity(orderItem.getQuantity())
                            .unitPrice(unitPrice)
                            .totalPrice(totalPrice)
                            .vatRate(vatRate.multiply(BigDecimal.valueOf(100)))
                            .vatAmount(vatAmount)
                            .orderNumber(orderNumber)
                            .orderDate(orderDate)
                            .build();

                    items.add(item);
                } catch (Exception e) {
                    log.error("Error processing order item for order {}: {}",
                            orderNumber, e.getMessage());
                }
            }
        }

        return items;
    }

    // Hàm lấy tên sản phẩm từ OrderItem
    private String getProductName(OrderItem orderItem) {
        try {
            ProductVariant variant = orderItem.getProductVariant();
            if (variant == null) {
                return "Unknown Product";
            }

            Product product = variant.getProduct();
            if (product == null) {
                return "Unknown Product";
            }

            // Translations đã được eager load
            String productName = "Unknown Product";
            if (product.getTranslations() != null && !product.getTranslations().isEmpty()) {
                productName = product.getTranslations().get(0).getName();
            }

            String color = variant.getColor() != null ? variant.getColor() : "N/A";
            String sizeName = "N/A";

            Size size = orderItem.getSize();
            if (size != null) {
                sizeName = size.getSizeName();
            }

            return String.format("%s (%s - %s)", productName, color, sizeName);
        } catch (Exception e) {
            log.warn("Error getting product name for order item: {}", e.getMessage());
            return "Unknown Product";
        }
    }

    // Hàm tính tổng tiền hàng (chưa thuế)
    private BigDecimal calculateTotalAmount(List<VATSalesItemDto> items) {
        return items.stream()
                .map(VATSalesItemDto::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Hàm tính tổng tiền thuế VAT
    private BigDecimal calculateTotalVAT(List<VATSalesItemDto> items) {
        return items.stream()
                .map(VATSalesItemDto::vatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Hàm định dạng kỳ báo cáo
    private String formatPeriod(LocalDate startDate, LocalDate endDate) {
        // Mẫu: "Tháng ... năm .... / Quý ... Năm ..."
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Từ %s đến %s", startDate.format(formatter), endDate.format(formatter));
    }

    // ========================================================================
    //  EXCEL EXPORT FOR VAT SALES REPORT 01-1/GTGT
    // ========================================================================

    private byte[] createVATSalesExcelReport(VATSalesListReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Tạo sheet
            XSSFSheet sheet = workbook.createSheet("Bangkebanra_01-1_GTGT");

            // Tạo styles
            Map<String, CellStyle> styles = createExcelStyles(workbook);

            // Cài đặt trang in
            sheet.getPrintSetup().setLandscape(false);
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

            int rowNum = 0;

            rowNum = createHeader(sheet, styles, report, rowNum);
            rowNum = createTable(sheet, styles, report, rowNum);
            rowNum = createFooter(sheet, styles, report, rowNum);

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            // Áp dụng thiết lập in trên một trang
            applySinglePagePrintSettings(sheet);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating VAT sales report Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate VAT sales report Excel", e);
        }
    }

    // ========================================================================
    //  STYLES
    // ========================================================================

    private Map<String, CellStyle> createExcelStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        DataFormat df = workbook.createDataFormat();

        // Base font
        Font baseFont = workbook.createFont();
        baseFont.setFontName("Times New Roman");
        baseFont.setFontHeightInPoints((short) 11);

        // Bold font
        Font boldFont = workbook.createFont();
        boldFont.setFontName("Times New Roman");
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 11);

        // Big bold (tiêu đề)
        Font bigBoldFont = workbook.createFont();
        bigBoldFont.setFontName("Times New Roman");
        bigBoldFont.setBold(true);
        bigBoldFont.setFontHeightInPoints((short) 13);

        // Italic font
        Font italicFont = workbook.createFont();
        italicFont.setFontName("Times New Roman");
        italicFont.setItalic(true);
        italicFont.setFontHeightInPoints((short) 11);

        // noBorderLeft
        CellStyle noBorderLeft = workbook.createCellStyle();
        noBorderLeft.setFont(baseFont);
        noBorderLeft.setAlignment(HorizontalAlignment.LEFT);
        noBorderLeft.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("noBorderLeft", noBorderLeft);

        // noBorderRight
        CellStyle noBorderRight = workbook.createCellStyle();
        noBorderRight.setFont(baseFont);
        noBorderRight.setAlignment(HorizontalAlignment.RIGHT);
        noBorderRight.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("noBorderRight", noBorderRight);

        // noBorderCenter
        CellStyle noBorderCenter = workbook.createCellStyle();
        noBorderCenter.setFont(baseFont);
        noBorderCenter.setAlignment(HorizontalAlignment.CENTER);
        noBorderCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("noBorderCenter", noBorderCenter);

        // title
        CellStyle title = workbook.createCellStyle();
        title.setFont(bigBoldFont);
        title.setAlignment(HorizontalAlignment.CENTER);
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("title", title);

        // header (ô tiêu đề cột)
        CellStyle header = workbook.createCellStyle();
        header.setFont(boldFont);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setBorderTop(BorderStyle.THIN);
        header.setBorderBottom(BorderStyle.THIN);
        header.setBorderLeft(BorderStyle.THIN);
        header.setBorderRight(BorderStyle.THIN);
        header.setWrapText(true);
        styles.put("header", header);

        // headerIndex [1],[2]
        CellStyle headerIndex = workbook.createCellStyle();
        headerIndex.cloneStyleFrom(header);
        headerIndex.setFont(baseFont);
        styles.put("headerIndex", headerIndex);

        // dataLeft
        CellStyle dataLeft = workbook.createCellStyle();
        dataLeft.setFont(baseFont);
        dataLeft.setAlignment(HorizontalAlignment.LEFT);
        dataLeft.setVerticalAlignment(VerticalAlignment.CENTER);
        dataLeft.setBorderTop(BorderStyle.THIN);
        dataLeft.setBorderBottom(BorderStyle.THIN);
        dataLeft.setBorderLeft(BorderStyle.THIN);
        dataLeft.setBorderRight(BorderStyle.THIN);
        styles.put("dataLeft", dataLeft);

        // dataCenter
        CellStyle dataCenter = workbook.createCellStyle();
        dataCenter.cloneStyleFrom(dataLeft);
        dataCenter.setAlignment(HorizontalAlignment.CENTER);
        styles.put("dataCenter", dataCenter);

        // number
        CellStyle number = workbook.createCellStyle();
        number.cloneStyleFrom(dataLeft);
        number.setAlignment(HorizontalAlignment.RIGHT);
        number.setDataFormat(df.getFormat("#,##0"));
        styles.put("number", number);

        // currency
        CellStyle currency = workbook.createCellStyle();
        currency.cloneStyleFrom(dataLeft);
        currency.setAlignment(HorizontalAlignment.RIGHT);
        currency.setDataFormat(df.getFormat("#,##0"));
        styles.put("currency", currency);

        // groupTitle (dòng: 1. Hàng hóa, dịch vụ ...)
        CellStyle groupTitle = workbook.createCellStyle();
        groupTitle.setFont(boldFont);
        groupTitle.setAlignment(HorizontalAlignment.LEFT);
        groupTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        groupTitle.setBorderTop(BorderStyle.THIN);
        groupTitle.setBorderBottom(BorderStyle.THIN);
        groupTitle.setBorderLeft(BorderStyle.THIN);
        groupTitle.setBorderRight(BorderStyle.THIN);
        styles.put("groupTitle", groupTitle);

        // groupTotal (dòng Tổng trong nhóm)
        CellStyle groupTotal = workbook.createCellStyle();
        groupTotal.setFont(boldFont);
        groupTotal.setAlignment(HorizontalAlignment.LEFT);
        groupTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        groupTotal.setBorderTop(BorderStyle.THIN);
        groupTotal.setBorderBottom(BorderStyle.THIN);
        groupTotal.setBorderLeft(BorderStyle.THIN);
        groupTotal.setBorderRight(BorderStyle.THIN);
        styles.put("groupTotal", groupTotal);

        // groupTotalNumber
        CellStyle groupTotalNumber = workbook.createCellStyle();
        groupTotalNumber.cloneStyleFrom(groupTotal);
        groupTotalNumber.setAlignment(HorizontalAlignment.RIGHT);
        groupTotalNumber.setDataFormat(df.getFormat("#,##0"));
        styles.put("groupTotalNumber", groupTotalNumber);

        // italicCenterNoBorder
        CellStyle italicCenterNoBorder = workbook.createCellStyle();
        italicCenterNoBorder.setFont(italicFont);
        italicCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
        italicCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("italicCenterNoBorder", italicCenterNoBorder);

        return styles;
    }

    // ========================================================================
    //  HEADER FOR VAT SALES REPORT 01-1/GTGT
    // ========================================================================

    private int createHeader(XSSFSheet sheet, Map<String, CellStyle> styles,
                             VATSalesListReport report, int rowNum) {

        // Row 0: Mẫu số
        Row r0 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        Cell c0 = r0.createCell(0);
        c0.setCellValue("Mẫu số: 01-1/GTGT");
        c0.setCellStyle(styles.get("noBorderLeft"));

        // Row 1: Thông tư
        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));
        Cell c1 = r1.createCell(0);
        c1.setCellValue("(Ban hành kèm theo Thông tư số 119/2014/TT-BTC ngày ....../....../........)");
        c1.setCellStyle(styles.get("noBorderLeft"));

        // Row 2: Người nộp thuế
        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));
        Cell c2 = r2.createCell(0);
        c2.setCellValue("Người nộp thuế: ...............................................................");
        c2.setCellStyle(styles.get("noBorderLeft"));

        // Row 3: Mã số thuế
        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));
        Cell c3 = r3.createCell(0);
        c3.setCellValue("Mã số thuế: .................................................................");
        c3.setCellStyle(styles.get("noBorderLeft"));

        // Row 4: trống
        rowNum++;

        // Row 5: Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 7));
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BẢNG KÊ HÓA ĐƠN, CHỨNG TỪ HÀNG HÓA, DỊCH VỤ BÁN RA");
        titleCell.setCellStyle(styles.get("title"));

        // Row 6: chú thích kèm tờ khai
        Row subTitleRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 7));
        Cell subCell = subTitleRow.createCell(0);
        subCell.setCellValue("(Kèm theo tờ khai thuế GTGT theo mẫu số 01/GTGT)");
        subCell.setCellStyle(styles.get("noBorderCenter"));

        // Row 7: Kỳ tính thuế
        Row periodRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 7));
        Cell pCell = periodRow.createCell(0);
        pCell.setCellValue("Kỳ tính thuế: " + report.reportPeriod());
        pCell.setCellStyle(styles.get("noBorderCenter"));

        // Row 8: Đơn vị tiền
        Row unitRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 5, 7));
        Cell uCell = unitRow.createCell(5);
        uCell.setCellValue("Đơn vị tiền: đồng Việt Nam");
        uCell.setCellStyle(styles.get("noBorderRight"));

        // Row 9: trống
        rowNum++;

        return rowNum;
    }

    // ========================================================================
    //  TABLE (4 NHÓM) FOR VAT SALES REPORT 01-1/GTGT
    // ========================================================================

    private int createTable(XSSFSheet sheet, Map<String, CellStyle> styles,
                            VATSalesListReport report, int rowNum) {

        // Header dòng 1
        Row h1 = sheet.createRow(rowNum++);
        String[] headerTexts = {
                "STT",
                "Số hóa đơn",
                "Ngày, tháng, năm\nlập hóa đơn",
                "Tên người mua",
                "Mã số thuế\nngười mua",
                "Doanh thu chưa có thuế GTGT",
                "Thuế GTGT",
                "Ghi chú"
        };

        for (int i = 0; i < headerTexts.length; i++) {
            Cell cell = h1.createCell(i);
            cell.setCellValue(headerTexts[i]);
            cell.setCellStyle(styles.get("header"));
        }

        // Header dòng 2: [1]..[8]
        Row h2 = sheet.createRow(rowNum++);
        for (int i = 0; i < headerTexts.length; i++) {
            Cell cell = h2.createCell(i);
            cell.setCellValue("[" + (i + 1) + "]");
            cell.setCellStyle(styles.get("headerIndex"));
        }

        // Chuẩn bị group data
        Map<VatGroup, List<VATSalesItemDto>> groupItems = new LinkedHashMap<>();
        groupItems.put(VatGroup.NON_TAX, new ArrayList<>());
        groupItems.put(VatGroup.VAT_0, new ArrayList<>());
        groupItems.put(VatGroup.VAT_5, new ArrayList<>());
        groupItems.put(VatGroup.VAT_10, new ArrayList<>());

        for (VATSalesItemDto item : report.items()) {
            VatGroup group = mapVatGroup(item);
            groupItems.get(group).add(item);
        }

        // Render từng nhóm
        rowNum = renderGroup(sheet, styles, rowNum,
                "1. Hàng hoá, dịch vụ không chịu thuế giá trị gia tăng (GTGT):",
                groupItems.get(VatGroup.NON_TAX));

        rowNum = renderGroup(sheet, styles, rowNum,
                "2. Hàng hoá, dịch vụ chịu thuế suất thuế GTGT 0%:",
                groupItems.get(VatGroup.VAT_0));

        rowNum = renderGroup(sheet, styles, rowNum,
                "3. Hàng hoá, dịch vụ chịu thuế suất thuế GTGT 5%:",
                groupItems.get(VatGroup.VAT_5));

        rowNum = renderGroup(sheet, styles, rowNum,
                "4. Hàng hoá, dịch vụ chịu thuế suất thuế GTGT 10%:",
                groupItems.get(VatGroup.VAT_10));

        return rowNum;
    }

    // Map item vào nhóm VAT
    private VatGroup mapVatGroup(VATSalesItemDto item) {
        BigDecimal rate = item.vatRate(); // % (0,5,10,...)
        if (rate == null) return VatGroup.NON_TAX;

        int cmpZero = rate.compareTo(BigDecimal.ZERO);
        if (cmpZero == 0) {
            // Không thuế hoặc 0% => tuỳ bạn muốn tách thế nào
            if (item.vatAmount() == null || item.vatAmount().compareTo(BigDecimal.ZERO) == 0) {
                return VatGroup.NON_TAX;
            } else {
                return VatGroup.VAT_0;
            }
        }

        if (rate.compareTo(new BigDecimal("5")) == 0) {
            return VatGroup.VAT_5;
        }
        if (rate.compareTo(new BigDecimal("10")) == 0) {
            return VatGroup.VAT_10;
        }

        // Default: coi như 10%
        return VatGroup.VAT_10;
    }

    // Render một nhóm dữ liệu
    private int renderGroup(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum, String groupTitleText, List<VATSalesItemDto> items) {
        // Dòng tiêu đề nhóm
        Row groupTitleRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        Cell gtCell = groupTitleRow.createCell(0);
        gtCell.setCellValue(groupTitleText);
        gtCell.setCellStyle(styles.get("groupTitle"));

        if (items.isEmpty()) {
            // Không có dữ liệu thì chỉ để trống 1 dòng phía dưới cho đẹp
            Row emptyRow = sheet.createRow(rowNum++);
            for (int i = 0; i < 8; i++) {
                Cell c = emptyRow.createCell(i);
                c.setCellStyle(styles.get("dataLeft"));
            }

            // Dòng Tổng (0)
            Row totalRow = sheet.createRow(rowNum++);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
            Cell tLabel = totalRow.createCell(0);
            tLabel.setCellValue("Tổng");
            tLabel.setCellStyle(styles.get("groupTotal"));

            Cell tAmount = totalRow.createCell(5);
            tAmount.setCellValue(0);
            tAmount.setCellStyle(styles.get("groupTotalNumber"));

            Cell tVat = totalRow.createCell(6);
            tVat.setCellValue(0);
            tVat.setCellStyle(styles.get("groupTotalNumber"));

            // cột ghi chú
            Cell tNote = totalRow.createCell(7);
            tNote.setCellStyle(styles.get("groupTotal"));

            return rowNum;
        }

        BigDecimal groupAmount = BigDecimal.ZERO;
        BigDecimal groupVat = BigDecimal.ZERO;

        // Dòng chi tiết
        for (VATSalesItemDto item : items) {
            Row row = sheet.createRow(rowNum++);

            int col = 0;

            Cell c0 = row.createCell(col++);
            c0.setCellValue(item.stt());
            c0.setCellStyle(styles.get("dataCenter"));

            Cell c1 = row.createCell(col++);
            c1.setCellValue(item.orderNumber());
            c1.setCellStyle(styles.get("dataLeft"));

            Cell c2 = row.createCell(col++);
            c2.setCellValue(item.orderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            c2.setCellStyle(styles.get("dataCenter"));

            Cell c3 = row.createCell(col++);
            c3.setCellValue(item.buyerName()); // ở đây bạn có thể đổi sang "tên người mua" nếu có
            c3.setCellStyle(styles.get("dataLeft"));

            Cell c4 = row.createCell(col++);
            c4.setCellValue(item.buyerTaxCode()); // Mã số thuế người mua – nếu có thì map vào đây
            c4.setCellStyle(styles.get("dataLeft"));

            Cell c5 = row.createCell(col++);
            c5.setCellValue(item.totalPrice().doubleValue());
            c5.setCellStyle(styles.get("currency"));

            Cell c6 = row.createCell(col++);
            c6.setCellValue(item.vatAmount().doubleValue());
            c6.setCellStyle(styles.get("currency"));

            Cell c7 = row.createCell(col++);
            c7.setCellValue(""); // Ghi chú
            c7.setCellStyle(styles.get("dataLeft"));

            groupAmount = groupAmount.add(item.totalPrice());
            groupVat = groupVat.add(item.vatAmount());
        }

        // Dòng Tổng
        Row totalRow = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        Cell tLabel = totalRow.createCell(0);
        tLabel.setCellValue("Tổng");
        tLabel.setCellStyle(styles.get("groupTotal"));

        Cell tAmount = totalRow.createCell(5);
        tAmount.setCellValue(groupAmount.doubleValue());
        tAmount.setCellStyle(styles.get("groupTotalNumber"));

        Cell tVat = totalRow.createCell(6);
        tVat.setCellValue(groupVat.doubleValue());
        tVat.setCellStyle(styles.get("groupTotalNumber"));

        Cell tNote = totalRow.createCell(7);
        tNote.setCellStyle(styles.get("groupTotal"));

        return rowNum;
    }

    // ========================================================================
    //  FOOTER FOR VAT SALES REPORT 01-1/GTGT
    // ========================================================================

    private int createFooter(XSSFSheet sheet, Map<String, CellStyle> styles,
                             VATSalesListReport report, int rowNum) {

        // Dòng trống
        rowNum++;

        // Tổng doanh thu chịu thuế
        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        Cell c10 = r1.createCell(0);
        c10.setCellValue("Tổng doanh thu hàng hoá, dịch vụ bán ra chịu thuế GTGT (*):");
        c10.setCellStyle(styles.get("noBorderLeft"));

        Cell c11 = r1.createCell(5);
        c11.setCellValue(report.totalAmount().doubleValue());
        c11.setCellStyle(styles.get("noBorderRight"));

        // Tổng số thuế GTGT
        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        Cell c20 = r2.createCell(0);
        c20.setCellValue("Tổng số thuế GTGT của hàng hoá, dịch vụ bán ra (**):");
        c20.setCellStyle(styles.get("noBorderLeft"));

        Cell c21 = r2.createCell(5);
        c21.setCellValue(report.totalVATAmount().doubleValue());
        c21.setCellStyle(styles.get("noBorderRight"));

        // Dòng trống
        rowNum++;

        // Dòng ngày tháng năm
        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));

        LocalDate today = LocalDate.now();
        Cell c30 = r3.createCell(4);
        c30.setCellValue(String.format("..........., ngày .... tháng .... năm %d", today.getYear()));
        c30.setCellStyle(styles.get("noBorderRight"));

        // Dòng "NGƯỜI NỘP THUẾ hoặc ĐẠI DIỆN HỢP PHÁP..."
        Row r4 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));
        Cell c40 = r4.createCell(4);
        c40.setCellValue("NGƯỜI NỘP THUẾ hoặc");
        c40.setCellStyle(styles.get("noBorderCenter"));

        Row r5 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));
        Cell c50 = r5.createCell(4);
        c50.setCellValue("ĐẠI DIỆN HỢP PHÁP CỦA NGƯỜI NỘP THUẾ");
        c50.setCellStyle(styles.get("noBorderCenter"));

        Row r6 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 4, 7));
        Cell c60 = r6.createCell(4);
        c60.setCellValue("Ký tên, đóng dấu (ghi rõ họ tên và chức vụ)");
        c60.setCellStyle(styles.get("italicCenterNoBorder"));

        return rowNum;
    }

    // Hàm sinh ngẫu nhiên mã số thuế (10 hoặc 13 số)
    private String randomTaxCode() {
        Random random = new Random();

        // 80% doanh nghiệp: MST 10 số — 20% cá nhân: MST 13 số
        int length = random.nextInt(100) < 80 ? 10 : 13;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // ========================================================================
    //  EXCEL EXPORT FOR VAT FORM 04/GTGT
    // ========================================================================
    private byte[] createVATForm04ExcelReport(LocalDate startDate, LocalDate endDate, VATSalesListReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("To_khai_04_GTGT");

            Map<String, CellStyle> styles = createExcelStyles(workbook);
            sheet.getPrintSetup().setLandscape(false);
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

            int rowNum = 0;

            // HEADER chung của mẫu 04/GTGT
            rowNum = createForm04Header(sheet, styles, report, rowNum);

            // Bảng A – khai thuế theo phương pháp trực tiếp trên doanh thu
            rowNum = createForm04TableA(sheet, styles, report, rowNum);

            // (Tuỳ em nếu muốn thêm phần B; với lựa chọn B, mình chỉ ghi 1 dòng “Không phát sinh”)
            rowNum = createForm04SectionB_NoData(sheet, styles, rowNum);

            // Chữ cam kết + chữ ký
            rowNum = createForm04Footer(sheet, styles, rowNum);

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            applySinglePagePrintSettings(sheet);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating 04/GTGT Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate 04/GTGT Excel", e);
        }
    }

    // ========================================================================
    //  HEADER FOR VAT FORM 04/GTGT
    // ========================================================================
    private int createForm04Header(XSSFSheet sheet,
                                   Map<String, CellStyle> styles,
                                   VATSalesListReport report,
                                   int rowNum) {

        // Mẫu số
        Row r0 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r0.getRowNum(), r0.getRowNum(), 4, 6));
        Cell c0 = r0.createCell(4);
        c0.setCellValue("Mẫu số: 04/GTGT");
        c0.setCellStyle(styles.get("noBorderRight"));

        // Thông tư
        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 2, 6));
        Cell c1 = r1.createCell(2);
        c1.setCellValue("(Ban hành kèm theo Thông tư số 80/2021/TT-BTC ngày 29/9/2021)");
        c1.setCellStyle(styles.get("noBorderCenter"));

        // Cộng hoà XHCN VN
        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 0, 6));
        Cell c2 = r2.createCell(0);
        c2.setCellValue("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM");
        c2.setCellStyle(styles.get("noBorderCenter"));

        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 0, 6));
        Cell c3 = r3.createCell(0);
        c3.setCellValue("Độc lập - Tự do - Hạnh phúc");
        c3.setCellStyle(styles.get("noBorderCenter"));

        // trống
        rowNum++;

        // Tên tờ khai
        Row r4 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r4.getRowNum(), r4.getRowNum(), 0, 6));
        Cell c4 = r4.createCell(0);
        c4.setCellValue("TỜ KHAI THUẾ GIÁ TRỊ GIA TĂNG");
        c4.setCellStyle(styles.get("title"));

        Row r5 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r5.getRowNum(), r5.getRowNum(), 0, 6));
        Cell c5 = r5.createCell(0);
        c5.setCellValue("(Áp dụng đối với người nộp thuế tính thuế theo phương pháp trực tiếp trên doanh thu)");
        c5.setCellStyle(styles.get("noBorderCenter"));

        // [01] Kỳ tính thuế
        Row r6 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r6.getRowNum(), r6.getRowNum(), 0, 6));
        Cell c6 = r6.createCell(0);
        c6.setCellValue("[01] Kỳ tính thuế: " + report.reportPeriod());
        c6.setCellStyle(styles.get("noBorderLeft"));

        // [02], [03] placeholder
        Row r7 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r7.getRowNum(), r7.getRowNum(), 0, 6));
        Cell c7 = r7.createCell(0);
        c7.setCellValue("[02] Lần đầu:      [03] Bổ sung lần thứ: ....");
        c7.setCellStyle(styles.get("noBorderLeft"));

        // [04] – [05] tạm để trống cho Kế toán điền tay
        Row r8 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r8.getRowNum(), r8.getRowNum(), 0, 6));
        Cell c8 = r8.createCell(0);
        c8.setCellValue("[04] Tên người nộp thuế: ...............................................................");
        c8.setCellStyle(styles.get("noBorderLeft"));

        Row r9 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r9.getRowNum(), r9.getRowNum(), 0, 6));
        Cell c9 = r9.createCell(0);
        c9.setCellValue("[05] Mã số thuế: ...............................................................");
        c9.setCellStyle(styles.get("noBorderLeft"));

        // trống
        rowNum++;

        // A. Khai thuế theo phương pháp trực tiếp trên doanh thu
        Row rA = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rA.getRowNum(), rA.getRowNum(), 0, 6));
        Cell cA = rA.createCell(0);
        cA.setCellValue("A. Khai thuế theo phương pháp trực tiếp trên doanh thu:");
        cA.setCellStyle(styles.get("noBorderLeft"));

        Row rUnit = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rUnit.getRowNum(), rUnit.getRowNum(), 0, 6));
        Cell cUnit = rUnit.createCell(0);
        cUnit.setCellValue("Đơn vị tiền: Đồng Việt Nam");
        cUnit.setCellStyle(styles.get("noBorderLeft"));

        // trống
        rowNum++;

        return rowNum;
    }

    // ========================================================================
    //  BẢNG A – KHAI THUẾ THEO PHƯƠNG PHÁP TRỰC TIẾP TRÊN DOANH THU
    // ========================================================================
    private int createForm04TableA(XSSFSheet sheet, Map<String, CellStyle> styles, VATSalesListReport report, int rowNum) {
        // Header
        Row h1 = sheet.createRow(rowNum++);
        String[] header = {
                "STT",
                "Nhóm ngành",
                "Doanh thu chịu thuế suất 0% và không chịu thuế\n([21],[24],[26],[28])",
                "Doanh thu chịu thuế GTGT (trừ thuế suất 0%)\n([22],[24],[26],[28])",
                "Thuế GTGT phải nộp\n([23],[25],[27],[29])"
        };

        for (int i = 0; i < header.length; i++) {
            Cell cell = h1.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(styles.get("header"));
        }

        // Tính số liệu
        BigDecimal totalTaxableRevenue = report.totalAmount() != null
                ? report.totalAmount()
                : BigDecimal.ZERO;

        BigDecimal group1NonTax = BigDecimal.ZERO; // hiện tại chưa tách
        BigDecimal group1Taxable = totalTaxableRevenue;
        BigDecimal group1Tax = group1Taxable.multiply(DIRECT_RATE_GROUP1)
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal group2NonTax = BigDecimal.ZERO;
        BigDecimal group2Taxable = BigDecimal.ZERO;
        BigDecimal group2Tax = BigDecimal.ZERO;

        BigDecimal group3NonTax = BigDecimal.ZERO;
        BigDecimal group3Taxable = BigDecimal.ZERO;
        BigDecimal group3Tax = BigDecimal.ZERO;

        BigDecimal group4NonTax = BigDecimal.ZERO;
        BigDecimal group4Taxable = BigDecimal.ZERO;
        BigDecimal group4Tax = BigDecimal.ZERO;

        BigDecimal sumNonTax = group1NonTax
                .add(group2NonTax)
                .add(group3NonTax)
                .add(group4NonTax);

        BigDecimal sumTaxable = group1Taxable
                .add(group2Taxable)
                .add(group3Taxable)
                .add(group4Taxable);

        BigDecimal sumTax = group1Tax
                .add(group2Tax)
                .add(group3Tax)
                .add(group4Tax);

        BigDecimal totalRevenue = sumNonTax.add(sumTaxable);

        // Row 1 – Phân phối, cung cấp hàng hoá
        rowNum = createForm04Row(
                sheet, styles, rowNum,
                1,
                "Phân phối, cung cấp hàng hoá ([23]=[22]x1%)",
                group1NonTax, group1Taxable, group1Tax
        );

        // Row 2 – Dịch vụ, xây dựng không bao thầu NVL
        rowNum = createForm04Row(
                sheet, styles, rowNum,
                2,
                "Dịch vụ, xây dựng không bao thầu nguyên vật liệu ([25]=[24]x5%)",
                group2NonTax, group2Taxable, group2Tax
        );

        // Row 3 – Sản xuất, vận tải, ...
        rowNum = createForm04Row(
                sheet, styles, rowNum,
                3,
                "Sản xuất, vận tải, dịch vụ có gắn với hàng hoá, xây dựng có bao thầu nguyên vật liệu ([27]=[26]x3%)",
                group3NonTax, group3Taxable, group3Tax
        );

        // Row 4 – Hoạt động kinh doanh khác
        rowNum = createForm04Row(
                sheet, styles, rowNum,
                4,
                "Hoạt động kinh doanh khác ([29]=[28]x2%)",
                group4NonTax, group4Taxable, group4Tax
        );

        // Row 5 – Doanh thu và số thuế phải nộp (30, 31)
        Row r5 = sheet.createRow(rowNum++);
        Cell c0 = r5.createCell(0);
        c0.setCellValue(5);
        c0.setCellStyle(styles.get("dataCenter"));

        Cell c1 = r5.createCell(1);
        c1.setCellValue("Doanh thu và số thuế phải nộp ([30]=[22]+[24]+[26]+[28]; [31]=[23]+[25]+[27]+[29])");
        c1.setCellStyle(styles.get("dataLeft"));

        Cell c2 = r5.createCell(2);
        c2.setCellValue(sumNonTax.doubleValue());
        c2.setCellStyle(styles.get("currency"));

        Cell c3 = r5.createCell(3);
        c3.setCellValue(sumTaxable.doubleValue());
        c3.setCellStyle(styles.get("currency"));

        Cell c4 = r5.createCell(4);
        c4.setCellValue(sumTax.doubleValue());
        c4.setCellStyle(styles.get("currency"));

        // Dòng "Tổng doanh thu ([32] =[21]+[30])"
        Row rTotal = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rTotal.getRowNum(), rTotal.getRowNum(), 0, 4));
        Cell cTotal = rTotal.createCell(0);
        cTotal.setCellValue(
                String.format("Tổng doanh thu ([32]=[21]+[30]): %s Đồng Việt Nam.",
                        totalRevenue.setScale(0, RoundingMode.HALF_UP).toPlainString())
        );
        cTotal.setCellStyle(styles.get("noBorderLeft"));

        // trống
        rowNum++;

        return rowNum;
    }

    // ========================================================================
    //  SUPPORTING METHODS FOR FORM 04/GTGT
    // ========================================================================
    private int createForm04Row(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum, int stt, String name, BigDecimal nonTaxRevenue, BigDecimal taxableRevenue, BigDecimal vat) {
        Row r = sheet.createRow(rowNum++);

        Cell c0 = r.createCell(0);
        c0.setCellValue(stt);
        c0.setCellStyle(styles.get("dataCenter"));

        Cell c1 = r.createCell(1);
        c1.setCellValue(name);
        c1.setCellStyle(styles.get("dataLeft"));

        Cell c2 = r.createCell(2);
        c2.setCellValue(nonTaxRevenue.doubleValue());
        c2.setCellStyle(styles.get("currency"));

        Cell c3 = r.createCell(3);
        c3.setCellValue(taxableRevenue.doubleValue());
        c3.setCellStyle(styles.get("currency"));

        Cell c4 = r.createCell(4);
        c4.setCellValue(vat.doubleValue());
        c4.setCellStyle(styles.get("currency"));

        return rowNum;
    }

    // ========================================================================
    //  SECTION B – KHAI RIÊNG ĐỐI VỚI KHOẢN THU HỘ
    // ========================================================================
    private int createForm04SectionB_NoData(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum) {

        Row rB = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rB.getRowNum(), rB.getRowNum(), 0, 6));
        Cell cB = rB.createCell(0);
        cB.setCellValue("B. Khai riêng đối với khoản thu hộ do cơ quan nhà nước có thẩm quyền giao:");
        cB.setCellStyle(styles.get("noBorderLeft"));

        Row rNote = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rNote.getRowNum(), rNote.getRowNum(), 0, 6));
        Cell cNote = rNote.createCell(0);
        cNote.setCellValue("Không phát sinh khoản thu hộ trong kỳ.");
        cNote.setCellStyle(styles.get("noBorderLeft"));

        // trống
        rowNum++;

        return rowNum;
    }

    // ========================================================================
    //  FOOTER FOR VAT FORM 04/GTGT
    // ========================================================================
    private int createForm04Footer(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum) {
        // Cam đoan
        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 0, 6));
        Cell c1 = r1.createCell(0);
        c1.setCellValue("Tôi cam đoan số liệu khai trên là đúng và chịu trách nhiệm trước pháp luật về số liệu đã khai.");
        c1.setCellStyle(styles.get("noBorderLeft"));

        // Ngày tháng năm + chữ ký
        LocalDate today = LocalDate.now();

        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 3, 6));
        Cell c2 = r2.createCell(3);
        c2.setCellValue(String.format("..., ngày .... tháng .... năm %d", today.getYear()));
        c2.setCellStyle(styles.get("noBorderCenter"));

        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 3, 6));
        Cell c3 = r3.createCell(3);
        c3.setCellValue("NGƯỜI NỘP THUẾ hoặc");
        c3.setCellStyle(styles.get("noBorderCenter"));

        Row r4 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r4.getRowNum(), r4.getRowNum(), 3, 6));
        Cell c4 = r4.createCell(3);
        c4.setCellValue("ĐẠI DIỆN HỢP PHÁP CỦA NGƯỜI NỘP THUẾ");
        c4.setCellStyle(styles.get("noBorderCenter"));

        Row r5 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r5.getRowNum(), r5.getRowNum(), 3, 6));
        Cell c5 = r5.createCell(3);
        c5.setCellValue("(Chữ ký, ghi rõ họ tên; chức vụ và đóng dấu (nếu có)/Ký điện tử)");
        c5.setCellStyle(styles.get("italicCenterNoBorder"));

        return rowNum;
    }

    // ========================================================================
    //  EXCEL EXPORT FOR VAT FORM 01-4A/GTGT
    // ========================================================================
    private byte[] createVATForm04AExcelReport(LocalDate startDate, LocalDate endDate, VATSalesListReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("01-4A_GTGT");

            Map<String, CellStyle> styles = createExcelStyles(workbook);

            int rowNum = 0;

            // HEADER
            rowNum = createForm04AHeader(sheet, styles, report, rowNum);

            // SECTION A
            rowNum = createForm04ASectionA(sheet, styles, report, rowNum);

            // SECTION B — Bảng phân bổ
            rowNum = createForm04ASectionB(sheet, styles, report, rowNum);

            // FOOTER
            rowNum = createForm04AFooter(sheet, styles, rowNum);

            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
            sheet.getPrintSetup().setLandscape(false);

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            applySinglePagePrintSettings(sheet);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating 01-4A/GTGT Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate", e);
        }
    }

    // ========================================================================
    //  HEADER FOR VAT FORM 01-4A/GTGT
    // ========================================================================
    private int createForm04AHeader(XSSFSheet sheet, Map<String, CellStyle> styles, VATSalesListReport report, int rowNum) {

        // Row: Mẫu số
        Row r0 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r0.getRowNum(), r0.getRowNum(), 4, 6));
        Cell c0 = r0.createCell(4);
        c0.setCellValue("Mẫu số: 01-4A/GTGT");
        c0.setCellStyle(styles.get("noBorderRight"));

        // Row: Thông tư
        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 2, 6));
        Cell c1 = r1.createCell(2);
        c1.setCellValue("(Ban hành kèm theo Thông tư số 28/2011/TT-BTC ngày 28/02/2011)");
        c1.setCellStyle(styles.get("noBorderCenter"));

        // PHỤ LỤC
        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 0, 6));
        Cell c2 = r2.createCell(0);
        c2.setCellValue("PHỤ LỤC");
        c2.setCellStyle(styles.get("title"));

        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 0, 6));
        Cell c3 = r3.createCell(0);
        c3.setCellValue("BẢNG PHÂN BỔ SỐ THUẾ GIÁ TRỊ GIA TĂNG");
        c3.setCellStyle(styles.get("title"));

        Row r4 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r4.getRowNum(), r4.getRowNum(), 0, 6));
        Cell c4 = r4.createCell(0);
        c4.setCellValue("CỦA HÀNG HÓA DỊCH VỤ MUA VÀO ĐƯỢC KHẤU TRỪ TRONG KỲ");
        c4.setCellStyle(styles.get("title"));

        Row r5 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r5.getRowNum(), r5.getRowNum(), 0, 6));
        Cell c5 = r5.createCell(0);
        c5.setCellValue("(Kèm theo tờ khai thuế GTGT mẫu số 01/GTGT)");
        c5.setCellStyle(styles.get("noBorderCenter"));

        // Mục chỉ tiêu 01 - 05
        rowNum++;

        Row r6 = sheet.createRow(rowNum++);
        Cell c6 = r6.createCell(0);
        c6.setCellValue("[01] Kỳ tính thuế: " + report.reportPeriod());
        c6.setCellStyle(styles.get("noBorderLeft"));

        Row r7 = sheet.createRow(rowNum++);
        Cell c7 = r7.createCell(0);
        c7.setCellValue("[02] Tên người nộp thuế: ...........................................................");
        c7.setCellStyle(styles.get("noBorderLeft"));

        Row r8 = sheet.createRow(rowNum++);
        Cell c8 = r8.createCell(0);
        c8.setCellValue("[03] Mã số thuế: ...........................................................");
        c8.setCellStyle(styles.get("noBorderLeft"));

        Row r9 = sheet.createRow(rowNum++);
        Cell c9 = r9.createCell(0);
        c9.setCellValue("[04] Tên đại lý thuế (nếu có): ...........................................................");
        c9.setCellStyle(styles.get("noBorderLeft"));

        Row r10 = sheet.createRow(rowNum++);
        Cell c10 = r10.createCell(0);
        c10.setCellValue("[05] Mã số thuế: ...........................................................");
        c10.setCellStyle(styles.get("noBorderLeft"));

        rowNum++;
        return rowNum;
    }

    // ========================================================================
    //  SECTION A FOR VAT FORM 01-4A/GTGT
    // ========================================================================
    private int createForm04ASectionA(XSSFSheet sheet,
                                      Map<String, CellStyle> styles,
                                      VATSalesListReport report,
                                      int rowNum) {

        Row rA = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rA.getRowNum(), rA.getRowNum(), 0, 6));
        Cell cA = rA.createCell(0);
        cA.setCellValue("A. Thuế GTGT của HHDV mua vào trong kỳ:");
        cA.setCellStyle(styles.get("noBorderLeft"));

        // Hiện chưa có dữ liệu thuế đầu vào ⇒ tạm để 0
        BigDecimal taxIn = report.totalVATAmount() != null ? report.totalVATAmount() : BigDecimal.ZERO;

        Row rA1 = sheet.createRow(rowNum++);
        Cell cA1 = rA1.createCell(0);
        cA1.setCellValue("… Số thuế: " + taxIn + " đồng.");
        cA1.setCellStyle(styles.get("noBorderLeft"));

        rowNum++;
        return rowNum;
    }

    // ========================================================================
    //  SECTION B FOR VAT FORM 01-4A/GTGT
    // ========================================================================
    private int createForm04ASectionB(XSSFSheet sheet, Map<String, CellStyle> styles, VATSalesListReport report, int rowNum) {

        Row rB = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(rB.getRowNum(), rB.getRowNum(), 0, 6));
        Cell cB = rB.createCell(0);
        cB.setCellValue("B. Phân bổ số thuế GTGT của HHDV mua vào được khấu trừ trong kỳ:");
        cB.setCellStyle(styles.get("noBorderLeft"));

        rowNum++;

        // HEADER
        Row h = sheet.createRow(rowNum++);
        String[] header = {"STT", "Chỉ tiêu", "Số tiền"};
        for (int i = 0; i < header.length; i++) {
            Cell cc = h.createCell(i);
            cc.setCellValue(header[i]);
            cc.setCellStyle(styles.get("header"));
        }

        // Values
        BigDecimal totalRevenue = report.totalAmount();
        BigDecimal taxable = report.totalAmount();  // tạm coi toàn bộ là chịu thuế 10%
        BigDecimal ratio = taxable.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : taxable.divide(totalRevenue, 4, RoundingMode.HALF_UP);

        BigDecimal vatIn = report.totalVATAmount();
        BigDecimal deductible = vatIn.multiply(ratio).setScale(0, RoundingMode.HALF_UP);

        rowNum = form04ARow(sheet, styles, rowNum, 1,
                "Tổng doanh thu hàng hóa, dịch vụ bán ra trong kỳ (1)",
                totalRevenue);

        rowNum = form04ARow(sheet, styles, rowNum, 2,
                "Doanh thu HHDV bán ra chịu thuế trong kỳ (2)",
                taxable);

        rowNum = form04ARow(sheet, styles, rowNum, 3,
                "Tỷ lệ % doanh thu chịu thuế (3) = (2)/(1)",
                ratio.multiply(BigDecimal.valueOf(100)));

        rowNum = form04ARow(sheet, styles, rowNum, 4,
                "Thuế GTGT mua vào cần phân bổ trong kỳ (4)",
                vatIn);

        rowNum = form04ARow(sheet, styles, rowNum, 5,
                "Thuế GTGT mua vào được khấu trừ (5) = (4)x(3)",
                deductible);

        rowNum++;
        return rowNum;
    }

    // ========================================================================
    //  SUPPORTING METHODS FOR FORM 01-4A/GTGT
    // ========================================================================
    private int form04ARow(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum, int stt, String label, BigDecimal value) {

        Row r = sheet.createRow(rowNum++);

        Cell c0 = r.createCell(0);
        c0.setCellValue(stt);
        c0.setCellStyle(styles.get("dataCenter"));

        Cell c1 = r.createCell(1);
        c1.setCellValue(label);
        c1.setCellStyle(styles.get("dataLeft"));

        Cell c2 = r.createCell(2);
        c2.setCellValue(value.doubleValue());
        c2.setCellStyle(styles.get("currency"));

        return rowNum;
    }

    // ========================================================================
    //  FOOTER FOR VAT FORM 01-4A/GTGT
    // ========================================================================
    private int createForm04AFooter(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum) {

        Row r1 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 0, 6));
        Cell c1 = r1.createCell(0);
        c1.setCellValue("Tôi cam đoan số liệu khai trên là đúng và chịu trách nhiệm trước pháp luật.");
        c1.setCellStyle(styles.get("noBorderLeft"));

        LocalDate today = LocalDate.now();

        Row r2 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 3, 6));
        Cell c2 = r2.createCell(3);
        c2.setCellValue("..., ngày .... tháng .... năm " + today.getYear());
        c2.setCellStyle(styles.get("noBorderCenter"));

        Row r3 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 3, 6));
        Cell c3 = r3.createCell(3);
        c3.setCellValue("NGƯỜI NỘP THUẾ hoặc ĐẠI DIỆN HỢP PHÁP");
        c3.setCellStyle(styles.get("noBorderCenter"));

        Row r4 = sheet.createRow(rowNum++);
        sheet.addMergedRegion(new CellRangeAddress(r4.getRowNum(), r4.getRowNum(), 3, 6));
        Cell c4 = r4.createCell(3);
        c4.setCellValue("(Ký, ghi rõ họ tên; chức vụ và đóng dấu (nếu có))");
        c4.setCellStyle(styles.get("italicCenterNoBorder"));

        return rowNum;
    }

    // Ham áp dụng thiết lập in 1 trang A4 dọc
    private void applySinglePagePrintSettings(XSSFSheet sheet) {

        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
        printSetup.setLandscape(false); // đứng
        printSetup.setFitHeight((short) 1); // chiều cao vừa đúng 1 trang
        printSetup.setFitWidth((short) 1);  // chiều ngang vừa đúng 1 trang
        printSetup.setScale((short) 90);    // thu nhỏ 90%, chỉnh tùy ý 70–100

        sheet.setFitToPage(true);

        // Set lề nhỏ để vừa trang
        sheet.setMargin(Sheet.TopMargin, 0.4);
        sheet.setMargin(Sheet.BottomMargin, 0.4);
        sheet.setMargin(Sheet.LeftMargin, 0.3);
        sheet.setMargin(Sheet.RightMargin, 0.3);

        // Nếu muốn loại dòng căn lề header/footer
        sheet.setMargin(Sheet.HeaderMargin, 0);
        sheet.setMargin(Sheet.FooterMargin, 0);

        // Bật gridlines khi in (hoặc tắt tùy em)
        sheet.setDisplayGridlines(true);
        sheet.setPrintGridlines(true);
    }
}

