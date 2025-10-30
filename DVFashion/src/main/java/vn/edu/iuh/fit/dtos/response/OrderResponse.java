/*
 * @ {#} OrderResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import vn.edu.iuh.fit.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Response DTO for order details
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
         Long id,

         String orderNumber,

         Long customerId,

         String customerName,

         OrderStatus status,

         BigDecimal subtotal,

         BigDecimal shippingFee,

         BigDecimal discountAmount,

         BigDecimal totalAmount,

         ShippingInfoResponse shippingInfo,

         String notes,

         LocalDateTime orderDate,


         LocalDateTime estimatedDeliveryTime,

         List<OrderItemResponse> items,

         PaymentResponse payment,

         PromotionOrderResponse promotion,

         String paypalApprovalUrl
) {}
