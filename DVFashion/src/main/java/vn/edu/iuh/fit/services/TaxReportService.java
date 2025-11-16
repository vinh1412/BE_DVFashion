/*
 * @ {#} TaxReportService.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.TaxReportResponse;

import java.time.LocalDate;

/*
 * @description: Service interface for VAT tax report operations
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
public interface TaxReportService {
    /**
     * Generates a VAT tax report for the specified date range.
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A TaxReportResponse containing the tax report data.
     */
    TaxReportResponse getVATTaxReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates an Excel file for the VAT tax report following Vietnamese format (01-1/GTGT).
     *
     * @param startDate The start date of the report range.
     * @param endDate   The end date of the report range.
     * @return A byte array representing the generated Excel file.
     */
    byte[] generateVATTaxReportExcel(LocalDate startDate, LocalDate endDate);
}
