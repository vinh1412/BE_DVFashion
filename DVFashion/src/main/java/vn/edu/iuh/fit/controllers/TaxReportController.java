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
import vn.edu.iuh.fit.services.TaxReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * @description: Controller for handling tax report requests
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/tax-reports")
public class TaxReportController {
    private final TaxReportService taxReportService;

    @GetMapping("/vat-form011/export/excel")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<byte[]> exportVATForm011Excel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] excelData = taxReportService.generateVATForm011Excel(startDate, endDate);

        String filename = String.format("BangKeHoaDonBanRa_01-1-GTGT_%s_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelData);
    }

    @GetMapping("/vat-form04/export/excel")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<byte[]> exportVATForm04Excel(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        byte[] excelData = taxReportService.generateVATForm04Excel(startDate, endDate);

        String filename = String.format(
                "ToKhaiThue_04_GTGT_%s_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelData);
    }

    @GetMapping("/vat-form014a/export/excel")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<byte[]> exportVATForm014AExcel(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        byte[] excelData = taxReportService.generateVATForm014AExcel(startDate, endDate);

        String filename = String.format(
                "BangPhanBoSoThue_01-4A_GTGT_%s_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelData);
    }
}
