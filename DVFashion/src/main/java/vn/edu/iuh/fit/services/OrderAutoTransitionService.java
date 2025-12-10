/*
 * @ {#} OrderAutoTransitionService.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.enums.AutoTransitionType;

/*
 * @description: Service interface for managing automatic order transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
public interface OrderAutoTransitionService {
    /**
     * Schedule an automatic transition for the specified order and transition type.
     *
     * @param order the order to schedule the transition for
     * @param transitionType the type of automatic transition
     */
    void scheduleAutoTransition(Order order, AutoTransitionType transitionType);

    /**
     * Execute all scheduled automatic order transitions that are due.
     */
    void executeScheduledTransitions();

    /**
     * Cancel all scheduled automatic transitions for the specified order and transition type.
     *
     * @param orderId the ID of the order
     * @param transitionType the type of automatic transition to cancel
     */
    void cancelScheduledTransitions(Long orderId, AutoTransitionType transitionType);
}
