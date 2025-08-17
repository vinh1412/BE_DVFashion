/*
 * @ {#} ProductImage.java   1.0     8/17/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;

/*
 * @description: Entity class representing an image associated with a product in the e-commerce system.
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
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "is_primary", columnDefinition = "boolean default true")
    private boolean isPrimary;

    @Column(name = "sort_order", columnDefinition = "int default 0")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

