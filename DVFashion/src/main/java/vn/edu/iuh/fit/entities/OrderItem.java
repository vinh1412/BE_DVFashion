/*
 * @ {#} OrderItem.java   1.0     8/17/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/*
 * @description: Entity class representing an item in an order.
 * @author: Nguyen Tan Thai Duong
 * @date:   8/17/2025
 * @version:    1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_product_variant_id", columnList = "product_variant_id"),
                @Index(name = "idx_order_item_size_id", columnList = "size_id"),
                @Index(name = "idx_order_item_variant_size", columnList = "product_variant_id, size_id")
        })
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productVariantId")
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sizeId")
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal discount;
}

