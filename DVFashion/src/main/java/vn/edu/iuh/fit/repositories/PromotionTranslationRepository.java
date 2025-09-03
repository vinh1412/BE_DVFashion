/*
 * @ {#} PromotionTranslationRepository.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.PromotionTranslation;
import vn.edu.iuh.fit.enums.Language;

import java.util.Optional;

/*
 * @description: Repository interface for PromotionTranslation entity
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
@Repository
public interface PromotionTranslationRepository extends JpaRepository<PromotionTranslation, Long> {
    /**
     * Check if a promotion translation exists by name (case insensitive) and language
     *
     * @param name     the name of the promotion translation
     * @param language the language of the promotion translation
     * @return true if a promotion translation with the given name and language exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);

    /**
     * Find a promotion translation by promotion ID and language
     *
     * @param promotionId the ID of the promotion
     * @param language    the language of the promotion translation
     * @return an Optional containing the found PromotionTranslation, or empty if not found
     */
    Optional<PromotionTranslation> findByPromotionIdAndLanguage(Long promotionId, Language language);
}
