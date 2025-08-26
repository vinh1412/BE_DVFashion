/*
 * @ {#} PasswordResetTokenRepository.java   1.0     26/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.PasswordResetToken;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   26/08/2025
 * @version:    1.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Finds a PasswordResetToken entity by its token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the found PasswordResetToken, or empty if not found
     */
    Optional<PasswordResetToken> findByToken(String token);
}
