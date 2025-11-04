/*
 * @ {#} ShippingService.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.response.ShippingCalculationResponse;

/*
 * @description: Service interface for shipping calculations
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */

public interface ShippingService {
    /*
     * Calculate shipping cost based on the order request
     *
     * @param request the create order request containing shipping details
     * @return the shipping calculation response with fee and estimated delivery time
     */
    ShippingCalculationResponse calculateShipping(CreateOrderRequest request);
}