/*
 * @ {#} StatisticServiceImpl.java   1.0     05/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.Inventory;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.mappers.InventoryMapper;
import vn.edu.iuh.fit.repositories.InventoryRepository;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.repositories.PromotionRepository;
import vn.edu.iuh.fit.services.StatisticService;
import vn.edu.iuh.fit.utils.LanguageUtils;

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

    private final InventoryRepository inventoryRepository;

    private final InventoryMapper inventoryMapper;

    private final PromotionRepository promotionRepository;

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
        LocalDate today = LocalDate.now();

        if (startDate == null && endDate == null) {
            startDate = today;
            endDate = today;
        } else if (startDate == null) {
            // If only endDate is passed → get 7 days before endDate
            startDate = endDate.minusDays(6);
        } else if (endDate == null) {
            // If only pass startDate → get up to today
            endDate = today;
        }

        // Convert LocalDate to LocalDateTime
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
    public List<RevenueDataPoint> getMonthlyRevenue(Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        List<Object[]> results = orderRepository.calculateMonthlyRevenue(OrderStatus.DELIVERED, targetYear);
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

    @Override
    public List<ProductSalesStatistic> getTop10BestSellingProducts() {
        Language language= LanguageUtils.getCurrentLanguage();
        List<Object[]> results = orderRepository.findBestSellingProductsByLanguage(OrderStatus.DELIVERED, language);

        return results.stream()
                .map(result -> new ProductSalesStatistic(
                        ((Number) result[0]).longValue(), // Product ID
                        (String) result[1],               // Product Name
                        ((Number) result[2]).longValue(), // Total Quantity
                        (BigDecimal) result[3]            // Total Revenue
                ))
                .toList();
    }

    @Override
    public List<ProductStockStatistic> getTopStockProducts(int limit) {
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> results = inventoryRepository.findTopStockProductsByLanguage(currentLanguage, pageable);

        return results.stream()
                .map(result -> new ProductStockStatistic(
                        ((Number) result[0]).longValue(), // productId
                        (String) result[1],               // productName
                        ((Number) result[2]).longValue()  // totalAvailableQuantity
                ))
                .toList();
    }

    @Override
    public List<InventoryResponse> getLowStockItems(int limit) {
        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Create Pageable object to limit results
        Pageable pageable = PageRequest.of(0, limit);

        // Find items with inventory below the minimum level
        List<Inventory> lowStockInventories = inventoryRepository.findLowStockItems(pageable);

        log.info("Retrieved top {} low stock items", lowStockInventories.size());
        return inventoryMapper.mapToInventoryResponseList(lowStockInventories, currentLanguage);
    }

    @Override
    public List<PromotionRevenueStatistic> getTopPromotionsByRevenue(int limit) {
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> results = promotionRepository.findTopPromotionsByRevenue(currentLanguage, pageable);

        return results.stream()
                .map(result -> new PromotionRevenueStatistic(
                        ((Number) result[0]).longValue(),  // promotionId
                        (String) result[1],                // promotionName
                        (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0 // totalRevenue
                ))
                .toList();
    }
}
