/*
 * @ {#} BrandTranslationRepository.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.entities.BrandTranslation;
import vn.edu.iuh.fit.enums.Language;

import java.util.Optional;

/*
 * @description: Repository interface for managing BrandTranslation entities
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public interface BrandTranslationRepository extends JpaRepository<BrandTranslation, Long> {
    /**
     * Finds a BrandTranslation by brand ID and language.
     *
     * @param brandId  the ID of the brand
     * @param language the language of the translation
     * @return an Optional containing the found BrandTranslation, or empty if not found
     */
    Optional<BrandTranslation> findByBrandIdAndLanguage(Long brandId, Language language);

    /**
     * Checks if a BrandTranslation with the given name and language exists, ignoring case.
     *
     * @param name     the name of the brand translation to check
     * @param language the language of the brand translation to check
     * @return true if a BrandTranslation with the given name and language exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);
}
