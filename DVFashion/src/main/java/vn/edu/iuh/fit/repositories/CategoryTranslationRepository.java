/*
 * @ {#} CategoryTranslationRepository.java   1.0     25/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.CategoryTranslation;
import vn.edu.iuh.fit.enums.Language;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */
@Repository
public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {
    Optional<CategoryTranslation> findByCategoryIdAndLanguage(Long categoryId, Language language);

    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);
}
