/*
 * @ {#} InternalRevenueTimeSeriesService.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;
import java.time.LocalDate;
import java.util.List;

/*
 * @description: Service interface for internal revenue time series operations.
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
public interface InternalRevenueTimeSeriesService {
    /**
     * Retrieves full daily revenue data points between the specified start and end dates.
     *
     * @param start The start date.
     * @param end   The end date.
     * @return A list of RevenueDataPoint representing daily revenue.
     */
    List<RevenueDataPoint> getFullDailyRevenue(LocalDate start, LocalDate end);
}
