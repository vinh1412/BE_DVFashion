/*
 * @ {#} ForecastingService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;

import java.util.List;

/*
 * @description: Service interface for revenue forecasting operations
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface ForecastingService {
    /**
     * Generates a revenue forecast for the specified number of days into the future.
     *
     * @param days The number of days to forecast.
     * @return A list of RevenueDataPoint representing the forecasted revenue.
     */
    List<RevenueDataPoint> getRevenueForecast(int days);

    /**
     * Triggers the retraining of the forecasting model.
     */
    void triggerModelRetraining();
}
