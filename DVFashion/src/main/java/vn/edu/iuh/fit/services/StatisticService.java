/*
 * @ {#} StatisticService.java   1.0     05/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: Service interface for statistical operations.
 * @author: Tran Hien Vinh
 * @date:   05/11/2025
 * @version:    1.0
 */
public interface StatisticService {
    /**
     * Calculate total revenue over a given period of time
     *
     * @param period    the period for which to calculate revenue (e.g., "day", "week", "month", "year", "range")
     * @param startDate the start date for the "range" period (optional)
     * @param endDate   the end date for the "range" period (optional)
     * @return total revenue as BigDecimal
     */
    BigDecimal getRevenueStatistics(String period, LocalDate startDate, LocalDate endDate);

    /**
     * Revenue statistics by day in the range startDate → endDate.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of RevenueDataPoint representing daily revenue
     */
    List<RevenueDataPoint> getDailyRevenue(LocalDate startDate, LocalDate endDate);

    /**
     * Revenue statistics by month in a specific year (e.g. 2025)
     *
     * @param year the year for which to calculate monthly revenue
     * @return list of RevenueDataPoint representing monthly revenue
     */
    List<RevenueDataPoint> getMonthlyRevenue(Integer year);

    /**
     * Total revenue statistics by year, based on delivered orders.
     *
     * @return list of RevenueDataPoint representing yearly revenue
     */
    List<RevenueDataPoint> getYearlyRevenue();

    /**
     * Get top 10 best-selling products based on quantity sold and total revenue.
     *
     * @return list of ProductSalesStatistic representing top 10 best-selling products
     */
    List<ProductSalesStatistic> getTop10BestSellingProducts();

    /**
     * Get top products with the highest stock quantity.
     *
     * @param limit the maximum number of products to retrieve
     * @return list of ProductStockStatistic representing top stock products
     */
    List<ProductStockStatistic> getTopStockProducts(int limit);

    /**
     * Get products with low stock quantity.
     *
     * @param limit the maximum number of products to retrieve
     * @return list of InventoryResponse representing low stock items
     */
    List<InventoryResponse> getLowStockItems(int limit);

    /**
     * Get top promotions based on total revenue generated from sold products.
     *
     * @param limit the maximum number of promotions to retrieve
     * @return list of PromotionRevenueStatistic representing top promotions by revenue
     */
    List<PromotionRevenueStatistic> getTopPromotionsByRevenue(int limit);
}
