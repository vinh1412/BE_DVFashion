/*
 * @ {#} TokenServiceImpl.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Token;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.TokenRefreshException;
import vn.edu.iuh.fit.repositories.TokenRepository;
import vn.edu.iuh.fit.services.TokenService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/*
 * @description: Service implementation for managing tokens
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    private final UserService userService;

    @Override
    public List<Token> findAllValidTokenByUser(Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        return tokenRepository.findAllValidTokenByUser(id);
    }

    @Override
    public Token findByRefreshTokenAndRevokedFalse(String refreshToken) {
        Token token = tokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database or is revoked"));
        return token;
    }

    @Override
    public void revokeToken(Token token) {
        if (token == null) {
            throw new NotFoundException("Token not found");
        }
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    @Scheduled(cron = "0 0 3 * * ?") // Every day at 3 AM
    @Transactional
    @Override
    public void cleanExpiredTokens() {
        Instant now = Instant.now();
        // Remove all expired tokens at the current time.
        tokenRepository.deleteAllExpiredSince(now);

        // Delete revoked tokens that have expired for more than 30 days
        Instant threshold = now.minus(30, ChronoUnit.DAYS);
        tokenRepository.deleteAllRevokedOlderThan(threshold);
    }
}
