/*
 * @ {#} StatisticServiceImpl.java   1.0     05/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.StatisticService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/*
 * @description: Implementation of the StatisticService interface.
 * @author: Tran Hien Vinh
 * @date:   05/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticService {
    private final OrderRepository orderRepository;

    @Override
    public BigDecimal getRevenueStatistics(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        LocalDate today = LocalDate.now();

        switch (period.toLowerCase()) {
            case "week":
                startDateTime = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                endDateTime = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);
                break;
            case "month":
                startDateTime = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDateTime = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                break;
            case "year":
                startDateTime = today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                endDateTime = today.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59);
                break;
            case "range":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Start date and end date are required for range period.");
                }
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atTime(23, 59, 59);
                break;
            case "day":
            default:
                startDateTime = today.atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                break;
        }

        BigDecimal revenue = orderRepository.calculateRevenue(OrderStatus.DELIVERED, startDateTime, endDateTime);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public List<RevenueDataPoint> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.calculateDailyRevenue(OrderStatus.DELIVERED, startDateTime, endDateTime);

        return results.stream()
                .map(result -> new RevenueDataPoint(
                        result[0].toString(), // Date
                        (BigDecimal) result[1] // Revenue
                ))
                .toList();
    }

    @Override
    public List<RevenueDataPoint> getMonthlyRevenue(int year) {
        List<Object[]> results = orderRepository.calculateMonthlyRevenue(OrderStatus.DELIVERED, year);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        return results.stream()
                .map(result -> {
                    LocalDateTime monthDateTime = (LocalDateTime) result[0];
                    String formattedPeriod = monthDateTime.format(formatter); // "2025-01", "2025-02", ...

                    BigDecimal revenue = (BigDecimal) result[1]; // Revenue
                    return new RevenueDataPoint(formattedPeriod, revenue);
                })
                .toList();
    }

    @Override
    public List<RevenueDataPoint> getYearlyRevenue() {
        List<Object[]> results = orderRepository.calculateYearlyRevenue(OrderStatus.DELIVERED);

        return results.stream()
                .map(result -> {
                    LocalDateTime yearDateTime = (LocalDateTime) result[0];
                    String year = String.valueOf(yearDateTime.getYear()); // "2025"

                    BigDecimal revenue = (BigDecimal) result[1]; // Revenue
                    return new RevenueDataPoint(year, revenue);
                })
                .toList();
    }
}
