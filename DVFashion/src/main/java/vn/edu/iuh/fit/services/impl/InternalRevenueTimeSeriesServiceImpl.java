/*
 * @ {#} InternalRevenueTimeSeriesServiceImpl.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.InternalRevenueTimeSeriesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * @description: Implementation of the InternalRevenueTimeSeriesService interface.
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class InternalRevenueTimeSeriesServiceImpl implements InternalRevenueTimeSeriesService {
    private final OrderRepository orderRepository;

    @Override
    public List<RevenueDataPoint> getFullDailyRevenue(LocalDate start, LocalDate end) {
        // Convert LocalDate to LocalDateTime for start and end of the day
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt   = end.atTime(23, 59, 59);

        // Fetch raw data from repository
        List<Object[]> rawList = orderRepository.fetchDailyRevenueSeries(
                OrderStatus.DELIVERED,
                startDt,
                endDt
        );

        // Insert into map to fill missing days
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rawList) {
            LocalDate day = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            map.put(day, revenue);
        }

        // Fill missing dates
        List<RevenueDataPoint> result = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            BigDecimal value = map.getOrDefault(cursor, BigDecimal.ZERO);
            result.add(new RevenueDataPoint(cursor.toString(), value));
            cursor = cursor.plusDays(1);
        }

        return result;
    }
}
