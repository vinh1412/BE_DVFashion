/*
 * @ {#} TokenService.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.jpa.repository.Query;
import vn.edu.iuh.fit.entities.Token;
import vn.edu.iuh.fit.entities.User;

import java.util.List;

/*
 * @description: Service interface for managing tokens
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public interface TokenService {
    /**
     * Find all valid tokens for a user by their ID.
     *
     * @param id the ID of the user
     * @return a list of valid tokens for the user
     */
    List<Token> findAllValidTokenByUser(Long id);

    /**
     * Find a token by its refresh token and check if it is not revoked.
     *
     * @param refreshToken the refresh token to search for
     * @return an Optional containing the Token if found and not revoked, or empty if not found
     */
    Token findByRefreshTokenAndRevokedFalse(String refreshToken);

    /**
     * Revoke a token.
     *
     * @param token the token to be revoked
     */
    void revokeToken(Token token);

    /**
     * Clean up expired tokens from the database.
     * This method should be scheduled to run periodically to ensure that expired tokens are removed.
     */
    void cleanExpiredTokens();

    /**
     * Save a refresh token for a user.
     *
     * @param user the user for whom the refresh token is being saved
     * @param refreshToken the refresh token to be saved
     */
    void saveRefreshToken(User user, String refreshToken);
}
