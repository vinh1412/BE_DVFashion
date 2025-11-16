/*
 * @ {#} RevenueReportService.java   1.0     15/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.RevenueReportResponse;
import vn.edu.iuh.fit.enums.ReportPeriodType;

import java.time.LocalDate;

/*
 * @description: Service interface for revenue report operations
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
public interface RevenueReportService {
    /**
     * Generates a revenue report for the specified period and date range.
     *
     * @param periodType The type of period for the report (e.g., DAILY, MONTHLY).
     * @param startDate  The start date of the report range.
     * @param endDate    The end date of the report range.
     * @return A RevenueReportResponse containing the report data.
     */
    RevenueReportResponse getRevenueReport(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate);

    /**
     * Generates an Excel file for the revenue report for the specified period and date range.
     *
     * @param periodType The type of period for the report (e.g., DAILY, MONTHLY).
     * @param startDate  The start date of the report range.
     * @param endDate    The end date of the report range.
     * @return A byte array representing the generated Excel file.
     */
    byte[] generateRevenueReportExcel(ReportPeriodType periodType, LocalDate startDate, LocalDate endDate);
}
