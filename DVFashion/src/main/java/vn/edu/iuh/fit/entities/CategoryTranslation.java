/*
 * @ {#} CategoryTranslation.java   1.0     25/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.Language;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "category_translations")
public class CategoryTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
