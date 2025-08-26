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
 * @description: Repository interface for managing CategoryTranslation entities
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */
@Repository
public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {
    /**
     * Finds a CategoryTranslation by category ID and language.
     *
     * @param categoryId the ID of the category
     * @param language   the language of the translation
     * @return an Optional containing the found CategoryTranslation, or empty if not found
     */
    Optional<CategoryTranslation> findByCategoryIdAndLanguage(Long categoryId, Language language);

    /**
     * Checks if a CategoryTranslation with the given name and language exists, ignoring case.
     *
     * @param name     the name of the category translation to check
     * @param language the language of the category translation to check
     * @return true if a CategoryTranslation with the given name and language exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);
}
