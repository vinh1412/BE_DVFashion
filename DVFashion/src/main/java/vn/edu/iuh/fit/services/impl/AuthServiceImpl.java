/*
 * @ {#} AuthServiceImpl.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.RefreshTokenRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.entities.Token;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.TokenRefreshException;
import vn.edu.iuh.fit.repositories.TokenRepository;
import vn.edu.iuh.fit.security.jwt.JwtUtils;
import vn.edu.iuh.fit.security.UserDetailsImpl;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.TokenService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for handling authentication operations
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final TokenRepository tokenRepository;

    private final TokenService tokenService;

    private final JwtUtils jwtUtils;

    @Override
    public boolean signUpForCustomer(SignUpRequest signUpRequest) {
        User user = userService.createCustomer(signUpRequest);
        if (user == null) {
            return false;
        }
        return true;
    }

    @Override
    public SignInResponse signIn(SignInRequest signInRequest) {
        // Check if username exists
        String username = FormatPhoneNumber.normalizePhone(signInRequest.getUsername());
        boolean existsByUsername = userService.existsByUsername(username);
        if (!existsByUsername) {
            throw new NotFoundException("Email or phone number not already exists. Please sign up first.");
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        // Set authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create user principal and generate tokens
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userPrincipal);
        String refreshToken = jwtUtils.generateRefreshToken(userPrincipal);

        // Get user roles
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Save refresh token in the database
        User user = userService.findById(userPrincipal.getId());
        saveRefreshToken(user, refreshToken);

        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .phone(userPrincipal.getPhone())
                .roles(roles)
                .build();
    }

    @Override
    public SignInResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Validate refresh token
        Token token = tokenService.findByRefreshTokenAndRevokedFalse(requestRefreshToken);

        // Check if refresh token is expired
        if (isTokenExpired(token.getExpirationDate())) {
            tokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        // Revoke the old refresh token
        tokenService.revokeToken(token);

        // Generate new access token and new refresh token
        User user = token.getUser();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Save the new refresh token in the database
        saveRefreshToken(user, newRefreshToken);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return SignInResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        // Find the token in the database
        Token token = tokenService.findByRefreshTokenAndRevokedFalse(refreshToken);

        // Revoke the token
        tokenService.revokeToken(token);
    }

    // Save the refresh token in the database
    private void saveRefreshToken(User user, String refreshToken) {
        // Revoke old tokens
        List<Token> validUserTokens = tokenService.findAllValidTokenByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> token.setRevoked(true));
            tokenRepository.saveAll(validUserTokens);
        }

        // Save new token
        Instant expirationDate = Instant.now().plus(7, ChronoUnit.DAYS); // 7 days expiration
        Token token = Token.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expirationDate(expirationDate)
                .isRevoked(false)
                .build();

        tokenRepository.save(token);
    }

    // Check if the token is expired
    private boolean isTokenExpired(Instant expirationDate) {
        return Instant.now().isAfter(expirationDate);
    }
}
