/*
 * @ {#} ShippingServiceImpl.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.CalculateLeadTimeRequest;
import vn.edu.iuh.fit.dtos.request.CalculateShippingRequest;
import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.response.ShippingCalculationResponse;
import vn.edu.iuh.fit.services.GhnService;
import vn.edu.iuh.fit.services.ShippingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/*
 * @description: Implementation of ShippingService using GHN integration
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {
    private final GhnService ghnService;

    // Default values for shipping calculations
    private static final Integer DEFAULT_FROM_DISTRICT_ID = 1461; // Quận Gò Vấp, TP.HCM
    private static final String DEFAULT_FROM_WARD_CODE = "21303"; // Phường 4, Quận Gò Vậy
    private static final Integer DEFAULT_SERVICE_ID = 53320; // Mã dịch vụ mặc định
    private static final Integer DEFAULT_SERVICE_TYPE_ID = 2; // Hàng nhẹ
    private static final Integer DEFAULT_WEIGHT = 1000; // 1kg
    private static final Integer DEFAULT_LENGTH = 10; // 10cm
    private static final Integer DEFAULT_WIDTH = 10; // 10
    private static final Integer DEFAULT_HEIGHT = 10; // 10cm

    @Override
    public ShippingCalculationResponse calculateShipping(CreateOrderRequest request) {
        try {
            // Get available service ID for the route
            Integer availableServiceId = ghnService.getAvailableServiceId(DEFAULT_FROM_DISTRICT_ID, request.shippingInfo().toDistrictId());

            // Prepare shipping calculation request
            CalculateShippingRequest shippingRequest = new CalculateShippingRequest(
                    availableServiceId != null ? availableServiceId : DEFAULT_SERVICE_ID,
                    DEFAULT_SERVICE_TYPE_ID,
                    request.shippingInfo().toWardCode(),
                    request.shippingInfo().toDistrictId(),
                    DEFAULT_FROM_DISTRICT_ID,
                    DEFAULT_WEIGHT,
                    DEFAULT_LENGTH,
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );

            // Calculate shipping fee
            BigDecimal shippingFee = ghnService.calculateShippingFee(shippingRequest);

            // Prepare lead time calculation request
            CalculateLeadTimeRequest leadTimeRequest = new CalculateLeadTimeRequest(
                    DEFAULT_FROM_DISTRICT_ID,
                    DEFAULT_FROM_WARD_CODE,
                    request.shippingInfo().toDistrictId(),
                    request.shippingInfo().toWardCode(),
                    DEFAULT_SERVICE_ID
            );

            // Calculate estimated delivery time
            LocalDateTime estimatedDeliveryTime = ghnService.calculateLeadTime(leadTimeRequest);

            // Format delivery time text
            String deliveryTimeText = formatDeliveryTime(estimatedDeliveryTime);

            return new ShippingCalculationResponse(
                    shippingFee,
                    estimatedDeliveryTime,
                    deliveryTimeText
            );
        } catch (Exception e) {
            log.error("Error calculating shipping: {}", e.getMessage());
            // Return default values if calculation fails
            return new ShippingCalculationResponse(
                    BigDecimal.valueOf(30000), // Default 30k VND
                    LocalDateTime.now().plusDays(3), // Default 3 days
                    "Nhận hàng trong 3-5 ngày"
            );
        }
    }

    // Format delivery time into user-friendly text
    private String formatDeliveryTime(LocalDateTime estimatedTime) {
        long daysFromNow = ChronoUnit.DAYS.between(LocalDateTime.now(), estimatedTime);
        if (daysFromNow <= 1) {
            return "Nhận hàng trong ngày";
        } else if (daysFromNow <= 2) {
            return "Nhận hàng trong 1-2 ngày";
        } else if (daysFromNow <= 5) {
            return "Nhận hàng trong 3-5 ngày";
        } else {
            return "Nhận hàng trong " + daysFromNow + " ngày";
        }
    }
}
