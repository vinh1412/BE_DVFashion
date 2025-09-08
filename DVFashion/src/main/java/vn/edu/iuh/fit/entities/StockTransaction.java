/*
 * @ {#} StockTransaction.java   1.0     9/6/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.iuh.fit.enums.StockTransactionType;

import java.time.LocalDateTime;

/*
 * @description: Entity class for StockTransaction
 * @author: Nguyen Tan Thai Duong
 * @date:   9/6/2025
 * @version:    1.0
 */
@Entity
@Table(name = "stock_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory; // Quan hệ Many-to-One với Inventory

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "order_id")
    private Long orderId; // Liên kết với Order (không dùng @JoinColumn)

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy; // Quan hệ Many-to-One với User

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

