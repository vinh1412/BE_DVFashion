/*
 * @ {#} RevenueReportService.java   1.0     15/11/2025
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
import vn.edu.iuh.fit.dtos.response.RevenueReportResponse;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.enums.ReportPeriodType;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.RevenueReportService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for revenue report operations
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueReportServiceImpl implements RevenueReportService {
    private final OrderRepository orderRepository;

    @Override
    public RevenueReportResponse getRevenueReport(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        OrderStatus status = OrderStatus.DELIVERED;

        // Tính toán metrics tổng quát bằng query - NHANH HỚN
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueInPeriod(status, startDateTime, endDateTime);
        long totalOrders = orderRepository.countOrdersInPeriod(status, startDateTime, endDateTime);
        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Lấy chi tiết theo period bằng query - NHANH HƠN
        List<RevenueReportResponse.RevenueDetailResponse> details =
                createRevenueDetailsOptimized(periodType, startDateTime, endDateTime, status);

        // So sánh với kỳ trước
        RevenueReportResponse.RevenueComparisonResponse comparison =
                createComparisonOptimized(totalRevenue, periodType, startDate, endDate);

        return RevenueReportResponse.builder()
                .reportTitle("BÁO CÁO DOANH THU " + periodType.getDisplayName().toUpperCase())
                .reportPeriod(formatPeriod(periodType, startDate, endDate))
                .fromDate(startDate)
                .toDate(endDate)
                .generatedDate(LocalDate.now())
                .totalRevenue(totalRevenue)
                .totalOrders(BigDecimal.valueOf(totalOrders))
                .averageOrderValue(averageOrderValue)
                .details(details)
                .comparison(comparison)
                .build();
    }

    private List<RevenueReportResponse.RevenueDetailResponse> createRevenueDetailsOptimized(
            ReportPeriodType periodType, LocalDateTime startDateTime, LocalDateTime endDateTime, OrderStatus status) {

        // Gọi query phù hợp theo periodType
        List<Object[]> queryResults = switch (periodType) {
            case DAILY -> orderRepository.getRevenueStatsByDay(status, startDateTime, endDateTime);
            case MONTHLY -> orderRepository.getRevenueStatsByMonth(status, startDateTime, endDateTime);
            case QUARTERLY -> orderRepository.getRevenueStatsByQuarter(status, startDateTime, endDateTime);
            case YEARLY -> orderRepository.getRevenueStatsByYear(status, startDateTime, endDateTime);
        };

        // Tạo map từ kết quả query
        Map<String, RevenueReportResponse.RevenueDetailResponse> resultMap = new LinkedHashMap<>();

        for (Object[] row : queryResults) {
            String period = (String) row[0];
            long totalOrders = ((Number) row[1]).longValue();
            BigDecimal totalRevenue = (BigDecimal) row[2];
            BigDecimal totalProducts = new BigDecimal(((Number) row[3]).toString());

            BigDecimal avgOrderValue = totalOrders > 0 ?
                    totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            resultMap.put(period, RevenueReportResponse.RevenueDetailResponse.builder()
                    .period(period)
                    .date(parsePeriodToDate(period, periodType))
                    .totalOrders(totalOrders)
                    .totalRevenue(totalRevenue)
                    .totalProducts(totalProducts)
                    .averageOrderValue(avgOrderValue)
                    .growthRate(null) // Sẽ tính sau
                    .build());
        }

        // Điền các period trống (nếu có)
        List<String> allPeriods = generatePeriods(periodType, startDateTime.toLocalDate(), endDateTime.toLocalDate());
        List<RevenueReportResponse.RevenueDetailResponse> details = new ArrayList<>();

        RevenueReportResponse.RevenueDetailResponse previousDetail = null;

        for (String period : allPeriods) {
            RevenueReportResponse.RevenueDetailResponse detail = resultMap.getOrDefault(period,
                    RevenueReportResponse.RevenueDetailResponse.builder()
                            .period(period)
                            .date(parsePeriodToDate(period, periodType))
                            .totalOrders(0L)
                            .totalRevenue(BigDecimal.ZERO)
                            .totalProducts(BigDecimal.ZERO)
                            .averageOrderValue(BigDecimal.ZERO)
                            .growthRate(null)
                            .build()
            );

            // Tính growth rate
            if (previousDetail != null && previousDetail.totalRevenue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growth = detail.totalRevenue().subtract(previousDetail.totalRevenue())
                        .divide(previousDetail.totalRevenue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                detail = RevenueReportResponse.RevenueDetailResponse.builder()
                        .period(detail.period())
                        .date(detail.date())
                        .totalOrders(detail.totalOrders())
                        .totalRevenue(detail.totalRevenue())
                        .totalProducts(detail.totalProducts())
                        .averageOrderValue(detail.averageOrderValue())
                        .growthRate(growth)
                        .build();
            }

            details.add(detail);
            previousDetail = detail;
        }

        return details;
    }

    // Method mới - tối ưu hóa comparison
    private RevenueReportResponse.RevenueComparisonResponse createComparisonOptimized(
            BigDecimal currentRevenue, ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {

        // Tính ngày của kỳ trước
        LocalDate previousStartDate;
        LocalDate previousEndDate;
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        switch (periodType) {
            case DAILY -> {
                previousStartDate = startDate.minusDays(daysBetween);
                previousEndDate = endDate.minusDays(daysBetween);
            }
            case MONTHLY -> {
                previousStartDate = startDate.minusMonths(1);
                previousEndDate = endDate.minusMonths(1);
            }
            case QUARTERLY -> {
                previousStartDate = startDate.minusMonths(3);
                previousEndDate = endDate.minusMonths(3);
            }
            case YEARLY -> {
                previousStartDate = startDate.minusYears(1);
                previousEndDate = endDate.minusYears(1);
            }
            default -> throw new IllegalStateException("Unexpected value: " + periodType);
        }

        // Dùng query để tính doanh thu kỳ trước - NHANH HƠN
        BigDecimal previousRevenue = orderRepository.calculateTotalRevenueInPeriod(
                OrderStatus.DELIVERED,
                previousStartDate.atStartOfDay(),
                previousEndDate.atTime(23, 59, 59)
        );

        // Tính toán tăng trưởng
        BigDecimal growthAmount = currentRevenue.subtract(previousRevenue);
        BigDecimal growthPercentage = BigDecimal.ZERO;
        String growthStatus = "Không thay đổi";

        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = growthAmount.divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (growthPercentage.compareTo(BigDecimal.ZERO) > 0) {
                growthStatus = "Tăng trưởng";
            } else if (growthPercentage.compareTo(BigDecimal.ZERO) < 0) {
                growthStatus = "Giảm";
            }
        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = BigDecimal.valueOf(100);
            growthStatus = "Tăng trưởng";
        }

        return RevenueReportResponse.RevenueComparisonResponse.builder()
                .previousPeriodRevenue(previousRevenue)
                .growthAmount(growthAmount)
                .growthPercentage(growthPercentage)
                .growthStatus(growthStatus)
                .build();
    }

    @Override
    public byte[] generateRevenueReportExcel(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
        RevenueReportResponse report = getRevenueReport(periodType, startDate, endDate);
        return createExcelReport(report, periodType);
    }

    private byte[] createExcelReport(RevenueReportResponse report, ReportPeriodType periodType) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Báo cáo doanh thu");

            // Set up Page Break Preview view
            sheet.getCTWorksheet()
                    .getSheetViews()
                    .getSheetViewArray(0)
                    .setView(org.openxmlformats.schemas.spreadsheetml.x2006.main.STSheetViewType.NORMAL);

            sheet.setAutobreaks(true);
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);


            // Create styles
            Map<String, CellStyle> styles = createExcelStyles(workbook);

            int rowNum = 0;

            // Header company
            rowNum = createCompanyHeader(sheet, styles, rowNum);

            // Report title
            rowNum = createReportTitle(sheet, styles, report, rowNum);

            // General information
            rowNum = createSummarySection(sheet, styles, report, rowNum);

            // Revenue details
            rowNum = createDetailSection(sheet, styles, report, periodType, rowNum);

            // Compare with previous period
            rowNum = createComparisonSection(sheet, styles, report, rowNum);

            // Footer
            createFooter(sheet, styles, rowNum);

            // Auto size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating revenue report Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate revenue report Excel", e);
        }
    }

    // Create Excel styles
    private Map<String, CellStyle> createExcelStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        // Style for company header
        CellStyle companyStyle = workbook.createCellStyle();
        Font companyFont = workbook.createFont();
        companyFont.setBold(true);
        companyFont.setFontHeightInPoints((short) 22);
        companyFont.setColor(IndexedColors.WHITE.getIndex());
        companyStyle.setFont(companyFont);
        companyStyle.setAlignment(HorizontalAlignment.CENTER);
        companyStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        companyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("company", companyStyle);

        // Style for report title
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("title", titleStyle);

        // Style for table headers
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);

        // Style for data cells
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        styles.put("data", dataStyle);

        // Style for currency
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0\" đ\""));
        styles.put("currency", currencyStyle);

        // Style for percentage
        CellStyle percentStyle = workbook.createCellStyle();
        percentStyle.cloneStyleFrom(dataStyle);
        percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        styles.put("percent", percentStyle);

        return styles;
    }

    // Create company header
    private int createCompanyHeader(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum) {
        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue("DVFASHION SHOP");
        companyCell.setCellStyle(styles.get("company"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        Row addressRow = sheet.createRow(rowNum++);
        Cell addressCell = addressRow.createCell(0);
        addressCell.setCellValue("12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP. Hồ Chí Minh");
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        Row contactRow = sheet.createRow(rowNum++);
        Cell contactCell = contactRow.createCell(0);
        contactCell.setCellValue("Phone: 0123456789 | Email: dvfashion@gmail.com");
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        // Empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    // Create report title section
    private int createReportTitle(XSSFSheet sheet, Map<String, CellStyle> styles, RevenueReportResponse report, int rowNum) {
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(report.reportTitle());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        Row periodRow = sheet.createRow(rowNum++);
        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue("Kỳ báo cáo: " + report.reportPeriod());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Ngày tạo báo cáo: " + report.generatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        // Empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    // Create summary section
    private int createSummarySection(XSSFSheet sheet, Map<String, CellStyle> styles, RevenueReportResponse report, int rowNum) {
        // Summary title
        Row summaryTitleRow = sheet.createRow(rowNum++);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("TỔNG QUAN");
        summaryTitleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        // Summary data
        String[][] summaryData = {
                {"Tổng doanh thu:", formatCurrency(report.totalRevenue())},
                {"Tổng số đơn hàng:", report.totalOrders().toString()},
                {"Giá trị đơn hàng trung bình:", formatCurrency(report.averageOrderValue())}
        };

        for (String[] data : summaryData) {
            Row row = sheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(data[0]);
            labelCell.setCellStyle(styles.get("data"));

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(data[1]);
            valueCell.setCellStyle(styles.get("data"));
        }

        // Empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    // Create detail section
    private int createDetailSection(XSSFSheet sheet, Map<String, CellStyle> styles, RevenueReportResponse report, ReportPeriodType periodType, int rowNum) {
        // Detail title
        Row detailTitleRow = sheet.createRow(rowNum++);
        Cell detailTitleCell = detailTitleRow.createCell(0);
        detailTitleCell.setCellValue("CHI TIẾT DOANH THU THEO " + periodType.getDisplayName().toUpperCase());
        detailTitleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Kỳ", "Số đơn hàng", "Tổng doanh thu", "Số sản phẩm", "ĐH trung bình", "Tăng trưởng (%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }

        // Data
        for (RevenueReportResponse.RevenueDetailResponse detail : report.details()) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(detail.period());
            row.getCell(0).setCellStyle(styles.get("data"));

            Cell ordersCell = row.createCell(1);
            ordersCell.setCellValue(detail.totalOrders());
            ordersCell.setCellStyle(styles.get("data"));

            Cell revenueCell = row.createCell(2);
            revenueCell.setCellValue(detail.totalRevenue().doubleValue());
            revenueCell.setCellStyle(styles.get("currency"));

            Cell productsCell = row.createCell(3);
            productsCell.setCellValue(detail.totalProducts().doubleValue());
            productsCell.setCellStyle(styles.get("data"));

            Cell avgCell = row.createCell(4);
            avgCell.setCellValue(detail.averageOrderValue().doubleValue());
            avgCell.setCellStyle(styles.get("currency"));

            Cell growthCell = row.createCell(5);
            if (detail.growthRate() != null) {
                growthCell.setCellValue(detail.growthRate().doubleValue() / 100);
                growthCell.setCellStyle(styles.get("percent"));
            } else {
                growthCell.setCellValue("-");
                growthCell.setCellStyle(styles.get("data"));
            }
        }

        // Empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    // Create comparison section
    private int createComparisonSection(XSSFSheet sheet, Map<String, CellStyle> styles, RevenueReportResponse report, int rowNum) {
        if (report.comparison() == null) return rowNum;

        // Comparison title
        Row comparisonTitleRow = sheet.createRow(rowNum++);
        Cell comparisonTitleCell = comparisonTitleRow.createCell(0);
        comparisonTitleCell.setCellValue("SO SÁNH VỚI KỲ TRƯỚC");
        comparisonTitleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

        // Comparison data
        RevenueReportResponse.RevenueComparisonResponse comparison = report.comparison();
        String[][] comparisonData = {
                {"Doanh thu kỳ trước:", formatCurrency(comparison.previousPeriodRevenue())},
                {"Doanh thu kỳ hiện tại:", formatCurrency(report.totalRevenue())},
                {"Chênh lệch:", formatCurrency(comparison.growthAmount())},
                {"Tỷ lệ tăng trưởng:", comparison.growthPercentage() + "%"},
                {"Tình trạng:", comparison.growthStatus()}
        };

        for (String[] data : comparisonData) {
            Row row = sheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(data[0]);
            labelCell.setCellStyle(styles.get("data"));

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(data[1]);
            valueCell.setCellStyle(styles.get("data"));
        }

        return rowNum + 2;
    }

    // Create footer
    private void createFooter(XSSFSheet sheet, Map<String, CellStyle> styles, int rowNum) {
        Row footerRow = sheet.createRow(rowNum);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("*** HẾT BÁO CÁO ***");
        CellStyle footerStyle = sheet.getWorkbook().createCellStyle();
        Font footerFont = sheet.getWorkbook().createFont();
        footerFont.setItalic(true);
        footerStyle.setFont(footerFont);
        footerStyle.setAlignment(HorizontalAlignment.CENTER);
        footerCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 7));
    }

    private BigDecimal calculateOrderRevenue(Order order) {
        BigDecimal productAmount = order.getItems().stream()
                .map(item -> item.getUnitPrice()
                        .subtract(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal voucherDiscount = order.getVoucherDiscount() != null ? order.getVoucherDiscount() : BigDecimal.ZERO;

        return productAmount.add(shippingFee).subtract(voucherDiscount);
    }

    // Calculate total revenue from orders
//    private BigDecimal calculateTotalRevenue(List<Order> orders) {
//        return orders.stream()
//                .map(this::calculateOrderRevenue)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }

    // Format currency
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f đ", amount);
    }

    // Format report period
    private String formatPeriod(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Từ %s đến %s", startDate.format(formatter), endDate.format(formatter));
    }

    // Create revenue details per period
//    private List<RevenueReportResponse.RevenueDetailResponse> createRevenueDetails(
//            List<Order> orders, ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
//
//        Map<String, List<Order>> groupedOrders = groupOrdersByPeriod(orders, periodType);
//        List<RevenueReportResponse.RevenueDetailResponse> details = new ArrayList<>();
//
//        // Create a list of periods by report type
//        List<String> periods = generatePeriods(periodType, startDate, endDate);
//
//        RevenueReportResponse.RevenueDetailResponse previousDetail = null;
//
//        for (String period : periods) {
//            List<Order> periodOrders = groupedOrders.getOrDefault(period, Collections.emptyList());
//
//            BigDecimal periodRevenue = calculateTotalRevenue(periodOrders);
//            long periodOrderCount = periodOrders.size();
//
//            // Calculate total products sold
//            BigDecimal totalProducts = periodOrders.stream()
//                    .flatMap(order -> order.getItems().stream())
//                    .map(item -> BigDecimal.valueOf(item.getQuantity()))
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            BigDecimal avgOrderValue = periodOrderCount > 0 ?
//                    periodRevenue.divide(BigDecimal.valueOf(periodOrderCount), 2, RoundingMode.HALF_UP) :
//                    BigDecimal.ZERO;
//
//            // Calculate growth rate
//            BigDecimal growthRate = null;
//            if (previousDetail != null && previousDetail.totalRevenue().compareTo(BigDecimal.ZERO) > 0) {
//                BigDecimal growth = periodRevenue.subtract(previousDetail.totalRevenue())
//                        .divide(previousDetail.totalRevenue(), 4, RoundingMode.HALF_UP)
//                        .multiply(BigDecimal.valueOf(100));
//                growthRate = growth;
//            }
//
//            RevenueReportResponse.RevenueDetailResponse detail = RevenueReportResponse.RevenueDetailResponse.builder()
//                    .period(period)
//                    .date(parsePeriodToDate(period, periodType))
//                    .totalOrders(periodOrderCount)
//                    .totalRevenue(periodRevenue)
//                    .totalProducts(totalProducts)
//                    .averageOrderValue(avgOrderValue)
//                    .growthRate(growthRate)
//                    .build();
//
//            details.add(detail);
//            previousDetail = detail;
//        }
//
//        return details;
//    }

    // Group orders by period
    private Map<String, List<Order>> groupOrdersByPeriod(List<Order> orders, ReportPeriodType periodType) {
        return orders.stream()
                .collect(Collectors.groupingBy(order -> formatOrderPeriod(order, periodType)));
    }

    // Format order date to period string
    private String formatOrderPeriod(Order order, ReportPeriodType periodType) {
        LocalDate orderDate = order.getOrderDate().toLocalDate();

        return switch (periodType) {
            case DAILY -> orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case MONTHLY -> orderDate.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            case QUARTERLY -> {
                int quarter = (orderDate.getMonthValue() - 1) / 3 + 1;
                yield String.format("Q%d/%d", quarter, orderDate.getYear());
            }
            case YEARLY -> String.valueOf(orderDate.getYear());
        };
    }

    // Generate list of periods between startDate and endDate
    private List<String> generatePeriods(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
        List<String> periods = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            periods.add(formatDateToPeriod(current, periodType));

            current = switch (periodType) {
                case DAILY -> current.plusDays(1);
                case MONTHLY -> current.plusMonths(1);
                case QUARTERLY -> current.plusMonths(3);
                case YEARLY -> current.plusYears(1);
            };
        }

        return periods;
    }

    // Format LocalDate to period string
    private String formatDateToPeriod(LocalDate date, ReportPeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case MONTHLY -> date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            case QUARTERLY -> {
                int quarter = (date.getMonthValue() - 1) / 3 + 1;
                yield String.format("Q%d/%d", quarter, date.getYear());
            }
            case YEARLY -> String.valueOf(date.getYear());
        };
    }

    // Parse period string back to LocalDate
    private LocalDate parsePeriodToDate(String period, ReportPeriodType periodType) {
        return switch (periodType) {
            case DAILY -> LocalDate.parse(period, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case MONTHLY -> LocalDate.parse("01/" + period, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case QUARTERLY -> {
                String[] parts = period.split("/");
                int quarter = Integer.parseInt(parts[0].substring(1));
                int year = Integer.parseInt(parts[1]);
                int month = (quarter - 1) * 3 + 1;
                yield LocalDate.of(year, month, 1);
            }
            case YEARLY -> LocalDate.of(Integer.parseInt(period), 1, 1);
        };
    }

    // Create comparison with previous period
//    private RevenueReportResponse.RevenueComparisonResponse createComparison(
//            BigDecimal currentRevenue, ReportPeriodType periodType, LocalDate startDate, LocalDate endDate) {
//
//        // Calculate previous period dates
//        LocalDate previousStartDate;
//        LocalDate previousEndDate;
//
//        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
//
//        switch (periodType) {
//            case DAILY -> {
//                previousStartDate = startDate.minusDays(daysBetween);
//                previousEndDate = endDate.minusDays(daysBetween);
//            }
//            case MONTHLY -> {
//                previousStartDate = startDate.minusMonths(1);
//                previousEndDate = endDate.minusMonths(1);
//            }
//            case QUARTERLY -> {
//                previousStartDate = startDate.minusMonths(3);
//                previousEndDate = endDate.minusMonths(3);
//            }
//            case YEARLY -> {
//                previousStartDate = startDate.minusYears(1);
//                previousEndDate = endDate.minusYears(1);
//            }
//            default -> throw new IllegalStateException("Unexpected value: " + periodType);
//        }
//
//        // Get orders for previous period
//        List<Order> previousOrders = orderRepository.findOrdersForComparison(
//                previousStartDate.atStartOfDay(),
//                previousEndDate.atTime(23, 59, 59),
//                List.of(OrderStatus.DELIVERED)
//        );
//
//        BigDecimal previousRevenue = calculateTotalRevenue(previousOrders);
//
//        // Calculate growth
//        BigDecimal growthAmount = currentRevenue.subtract(previousRevenue);
//
//        BigDecimal growthPercentage = BigDecimal.ZERO;
//        String growthStatus = "Không thay đổi";
//
//        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
//            growthPercentage = growthAmount.divide(previousRevenue, 4, RoundingMode.HALF_UP)
//                    .multiply(BigDecimal.valueOf(100));
//
//            if (growthPercentage.compareTo(BigDecimal.ZERO) > 0) {
//                growthStatus = "Tăng trưởng";
//            } else if (growthPercentage.compareTo(BigDecimal.ZERO) < 0) {
//                growthStatus = "Giảm";
//            }
//        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
//            growthPercentage = BigDecimal.valueOf(100);
//            growthStatus = "Tăng trưởng";
//        }
//
//        return RevenueReportResponse.RevenueComparisonResponse.builder()
//                .previousPeriodRevenue(previousRevenue)
//                .growthAmount(growthAmount)
//                .growthPercentage(growthPercentage)
//                .growthStatus(growthStatus)
//                .build();
//    }
}
