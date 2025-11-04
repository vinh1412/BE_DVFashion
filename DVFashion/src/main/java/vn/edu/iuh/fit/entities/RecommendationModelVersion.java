/*
 * @ {#} RecommendationModelVersion.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * @description: Entity representing a recommendation model version
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
@Entity
@Table(name = "recommendation_model_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationModelVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "content_weight")
    private Double contentWeight;

    @Column(name = "collaborative_weight")
    private Double collaborativeWeight;

    @Column(name = "precision_at_10")
    private Double precisionAt10;

    @Column(name = "recall_at_10")
    private Double recallAt10;

    @Column(name = "map_at_10")
    private Double mapAt10;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
