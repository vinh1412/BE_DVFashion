/*
 * @ {#} OrderAutoTransitionScheduler.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.services.OrderAutoTransitionService;

/*
 * @description: Scheduler for executing automatic order status transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Component
@ConditionalOnProperty(value = "order.auto-transition.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OrderAutoTransitionScheduler {

    private final OrderAutoTransitionService autoTransitionService;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void executeAutoTransitions() {
        try {
            log.debug("Starting auto transition execution");
            autoTransitionService.executeScheduledTransitions();
            log.debug("Completed auto transition execution");
        } catch (Exception e) {
            log.error("Error during auto transition execution: {}", e.getMessage(), e);
        }
    }
}
