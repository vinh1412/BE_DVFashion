/*
 * @ {#} TokenRepository.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Token;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing tokens in the system
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {
    /**
     * Find all valid tokens for a user by their ID.
     *
     * @param id the ID of the user
     * @return a list of valid tokens for the user
     */
    @Query(value = """
        SELECT t FROM Token t INNER JOIN User u ON t.user.id = u.id
        WHERE u.id = :id AND (t.isRevoked = false)
        """)
    List<Token> findAllValidTokenByUser(Long id);

    /**
     * Find a token by its refresh token, ensuring it is not revoked.
     *
     * @param refreshToken the refresh token to search for
     * @return an Optional containing the Token if found and not revoked, or empty if not found
     */
    @Query("SELECT t FROM Token t WHERE t.refreshToken = :refreshToken AND t.isRevoked = false")
    Optional<Token> findByRefreshTokenAndRevokedFalse(String refreshToken);


    /**
     * Delete all tokens that have expired since the specified time.
     *
     * @param now
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.expirationDate < :now")
    void deleteAllExpiredSince(@Param("now") Instant now);

    /**
     * Delete all revoked tokens that are older than the specified threshold.
     *
     * @param threshold the Instant before which revoked tokens will be deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.isRevoked = true AND t.expirationDate < :threshold")
    void deleteAllRevokedOlderThan(@Param("threshold") Instant threshold);

    void deleteByRefreshToken(String refreshToken);
}
