/*
 * @ {#} Inventory.java   1.0     9/6/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

/*
 * @description: Entity class for Inventory
 * @author: Nguyen Tan Thai Duong
 * @date:   9/6/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false, unique = true)
    private Size size;

    @Column(name = "quantity_in_stock")
    @Builder.Default
    private Integer quantityInStock = 0;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 5;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Quan hệ One-to-Many với StockTransaction
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockTransaction> stockTransactions = new ArrayList<>();

    public int getAvailableQuantity() {
        return quantityInStock - reservedQuantity;
    }
}

