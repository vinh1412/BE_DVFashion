/*
 * @ {#} ProductVariantRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ProductTranslation;
import vn.edu.iuh.fit.enums.Language;

import java.util.Optional;

/*
 * @description: Repository interface for managing Product Translation entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, Long> {
    /**
     * Find a product translation by product ID and language.
     *
     * @param productId the ID of the product
     * @param language  the language of the translation
     * @return an Optional containing the found ProductTranslation, or empty if not found
     */
    Optional<ProductTranslation> findByProductIdAndLanguage(Long productId, Language language);

    /**
     * Check if a product translation exists by name and language.
     *
     * @param name     the name of the product translation
     * @param language the language of the translation
     * @return true if a ProductTranslation with the given name and language exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);
}
