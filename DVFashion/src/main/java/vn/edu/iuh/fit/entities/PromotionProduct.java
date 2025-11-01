/*
 * @ {#} PromotionProduct.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "promotion_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "product_id"}))
public class PromotionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice; // Giá gốc

    @Column(name = "promotion_price", precision = 10, scale = 2)
    private BigDecimal promotionPrice; // Giá khuyến mãi

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage; // % giảm giá

    @Column(name = "stock_quantity")
    private Integer stockQuantity; // Số lượng hàng cho khuyến mãi

    @Column(name = "sold_quantity", columnDefinition = "int default 0")
    private int soldQuantity; // Số lượng đã bán trong khuyến mãi

    @Column(name = "max_quantity_per_user")
    private Integer maxQuantityPerUser; // Giới hạn mua tối đa mỗi user

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
