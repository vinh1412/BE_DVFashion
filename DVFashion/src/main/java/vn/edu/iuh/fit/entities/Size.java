/*
 * @ {#} Size.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sizes")
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "size_name", nullable = false)
    private String sizeName;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @OneToOne(mappedBy = "size", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;
}
