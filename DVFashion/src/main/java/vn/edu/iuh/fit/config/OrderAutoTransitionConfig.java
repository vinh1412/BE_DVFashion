/*
 * @ {#} OrderAutoTransitionConfig.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/*
 * @description: Configuration class for automatic order status transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Configuration
@ConfigurationProperties(prefix = "order.auto-transition")
@Getter
@Setter
public class OrderAutoTransitionConfig {

    private boolean enabled = true;

    // Cấu hình thời gian delay cho các transition
    private Map<String, Duration> delays = Map.of(
            "CONFIRMED_TO_PROCESSING", Duration.ofHours(2), // Auto process confirmed orders after 2 hours
            "PROCESSING_TO_SHIPPED", Duration.ofDays(1), // Auto ship processing orders after 1 day
            "SHIPPED_TO_DELIVERED", Duration.ofDays(3), // Auto deliver shipped orders after 3 days
            "PENDING_TO_CANCELLED", Duration.ofDays(7) // Auto cancel unpaid pending orders after 7 days

            // Cấu hình thời gian delay ngắn cho mục đích demo
//            "CONFIRMED_TO_PROCESSING", Duration.ofSeconds(5),  // Sau 5 giây từ CONFIRMED -> PROCESSING
//            "PROCESSING_TO_SHIPPED", Duration.ofSeconds(5),   // Sau 5 giây từ PROCESSING -> SHIPPED
//            "SHIPPED_TO_DELIVERED", Duration.ofSeconds(5)    // Sau 5 giây từ SHIPPED -> DELIVERED
//            "PENDING_TO_CANCELLED", Duration.ofSeconds(10)
    );

    // Cấu hình các điều kiện
    private boolean notifyCustomerOnTransition = false; // Gửi thông báo cho khách hàng khi có transition

    // Cấu hình business hours
    private int businessStartHour = 5; // 5 AM
    private int businessEndHour = 21; // 9 PM
    private boolean respectBusinessHours = false;
}
