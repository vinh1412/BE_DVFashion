/*
 * @ {#} OrderAutoTransition.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.AutoTransitionType;
import vn.edu.iuh.fit.enums.OrderStatus;

import java.time.LocalDateTime;

/*
 * @description: Entity class representing automatic order transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Entity
@Table(name = "order_auto_transitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAutoTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_type", nullable = false)
    private AutoTransitionType transitionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private OrderStatus toStatus;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "is_executed")
    private boolean isExecuted = false;

    @Column(name = "execution_result")
    private String executionResult;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
