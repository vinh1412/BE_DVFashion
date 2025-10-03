/*
 * @ {#} OrderService.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.response.OrderResponse;

/*
 * @description: Service interface for managing orders
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
public interface OrderService {
    /**
     * Create a new order based on the provided request.
     *
     * @param request the create order request containing order details
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);
}
