/*
 * @ {#} VoucherTranslationRepository.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.VoucherTranslation;
import vn.edu.iuh.fit.enums.Language;

import java.util.Optional;

/*
 * @description: Repository interface for VoucherTranslation entity
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Repository
public interface VoucherTranslationRepository extends JpaRepository<VoucherTranslation, Long> {
    /**
     * Find a VoucherTranslation by voucher ID and language.
     *
     * @param voucherId the ID of the voucher
     * @param language  the language of the translation
     * @return an Optional containing the VoucherTranslation if found, or empty if not found
     */
    Optional<VoucherTranslation> findByVoucherIdAndLanguage(Long voucherId, Language language);

    /**
     * Check if a VoucherTranslation exists by name (case insensitive) and language.
     *
     * @param name     the name of the voucher translation
     * @param language the language of the translation
     * @return true if a VoucherTranslation with the given name and language exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndLanguage(String name, Language language);
}
