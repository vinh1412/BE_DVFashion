/*
 * @ {#} Promotion.java   1.0     8/17/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.PromotionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Entity class representing a promotion in the system.
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
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromotionTranslation> translations = new ArrayList<>();
}

