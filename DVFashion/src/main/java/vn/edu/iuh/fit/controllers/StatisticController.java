/*
 * @ {#} StatisticController.java   1.0     05/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;
import vn.edu.iuh.fit.services.StatisticService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: Controller for handling statistical data endpoints
 * @author: Tran Hien Vinh
 * @date:   05/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/statistics")
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenueStatistics(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        BigDecimal revenue = statisticService.getRevenueStatistics(period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(revenue, "Revenue statistics retrieved successfully"));
    }


    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<List<RevenueDataPoint>>> getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<RevenueDataPoint> revenueList = statisticService.getDailyRevenue(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(revenueList, "Daily revenue retrieved successfully"));
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<List<RevenueDataPoint>>> getMonthlyRevenue(
            @RequestParam(required = false) Integer year
    ) {
        List<RevenueDataPoint> revenueList = statisticService.getMonthlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success(revenueList, "Monthly revenue retrieved successfully"));
    }

    @GetMapping("/revenue/yearly")
    public ResponseEntity<ApiResponse<List<RevenueDataPoint>>> getYearlyRevenue() {
        List<RevenueDataPoint> revenueList = statisticService.getYearlyRevenue();
        return ResponseEntity.ok(ApiResponse.success(revenueList, "Yearly revenue retrieved successfully"));
    }
}
