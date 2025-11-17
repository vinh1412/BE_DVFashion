/*
 * @ {#} TaxReportService1.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.TaxReportResponse;

import java.time.LocalDate;

/*
 * @description: Service interface for generating tax reports
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
public interface TaxReportService {
    /**
     * Generates a VAT sales report for the specified date range.
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A TaxReportResponse containing the VAT sales report data.
     */
    TaxReportResponse getVATSalesReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates an Excel file for the VAT sales report (Bảng kê bán ra 01-1/GTGT).
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A byte array representing the generated Excel file.
     */
    byte[] generateVATForm011Excel(LocalDate startDate, LocalDate endDate);

    /**
     * Generates an Excel file for the VAT Form 04 report.
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A byte array representing the generated Excel file for VAT Form 04.
     */
    byte[] generateVATForm04Excel(LocalDate startDate, LocalDate endDate);

    /**
     * Generates an Excel file for the VAT Form 04A report.
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A byte array representing the generated Excel file for VAT Form 04A.
     */
    byte[] generateVATForm014AExcel(LocalDate startDate, LocalDate endDate);
}
