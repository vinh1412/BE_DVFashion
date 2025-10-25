/*
 * @ {#} RecommendationLog.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/*
 * @description: This class represents a log entry for recommendations made by the system.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Entity
@Table(name = "recommendation_logs",
        indexes = {
                @Index(name = "idx_recommendation_user", columnList = "user_id"),
                @Index(name = "idx_recommendation_product", columnList = "product_id"),
                @Index(name = "idx_recommendation_recommended", columnList = "recommended_product_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "recommended_product_id", nullable = false)
    private Long recommendedProductId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
