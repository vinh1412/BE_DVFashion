/*
 * @ {#} OrderAutoTransitionRepository.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.OrderAutoTransition;
import vn.edu.iuh.fit.enums.AutoTransitionType;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Repository interface for automatic order transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Repository
public interface OrderAutoTransitionRepository extends JpaRepository<OrderAutoTransition, Long> {
    /**
     * Find all pending automatic transitions that are scheduled to be executed
     * at or before the specified time.
     *
     * @param now the current time
     * @return list of pending automatic transitions
     */
    @Query("""
        SELECT oat FROM OrderAutoTransition oat 
        WHERE oat.isExecuted = false 
        AND oat.scheduledAt <= :now
        ORDER BY oat.scheduledAt ASC
    """)
    List<OrderAutoTransition> findPendingTransitions(@Param("now") LocalDateTime now);

    /**
     * Find all pending automatic transitions for a specific order and transition type.
     *
     * @param orderId the ID of the order
     * @param transitionType the type of automatic transition
     * @return list of pending automatic transitions for the order and type
     */
    @Query("""
        SELECT oat FROM OrderAutoTransition oat 
        WHERE oat.order.id = :orderId 
        AND oat.transitionType = :transitionType 
        AND oat.isExecuted = false
    """)
    List<OrderAutoTransition> findPendingTransitionsByOrderAndType(
            @Param("orderId") Long orderId,
            @Param("transitionType") AutoTransitionType transitionType
    );

    /**
     * Check if a pending automatic transition exists for a specific order and transition type.
     *
     * @param orderId the ID of the order
     * @param transitionType the type of automatic transition
     * @return true if a pending transition exists, false otherwise
     */
    boolean existsByOrderIdAndTransitionTypeAndIsExecutedFalse(Long orderId, AutoTransitionType transitionType);
}
