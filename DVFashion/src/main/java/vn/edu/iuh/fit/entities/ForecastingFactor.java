/*
 * @ {#} ForecastingFactor.java   1.0     8/17/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;

/*
 * @description: Entity class representing a forecasting factor in the sales forecasting system.
 * @author: Nguyen Tan Thai Duong
 * @date:   8/17/2025
 * @version:    1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "forecasting_factors")
public class ForecastingFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factor_name", nullable = false)
    private String factorName;

    private double weight;

    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_forecasting_id", nullable = false)
    private SalesForecasting forecasting;
}

