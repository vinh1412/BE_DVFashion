/*
 * @ {#} TaxReportController.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.TaxReportResponse;
import vn.edu.iuh.fit.services.TaxReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * @description: Controller for handling VAT tax report requests
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/reports/tax")
@RequiredArgsConstructor
public class TaxReportController {

    private final TaxReportService taxReportService;

    @GetMapping("/vat")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<TaxReportResponse>> getVATTaxReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        TaxReportResponse report = taxReportService.getVATTaxReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/vat/export/excel")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<byte[]> exportVATTaxReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] excelData = taxReportService.generateVATTaxReportExcel(startDate, endDate);

        String filename = String.format("BangKeBanRa_01-1-GTGT_%s_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelData);
    }
}
