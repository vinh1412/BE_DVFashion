/*
 * @ {#} AuthServiceImpl.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Token;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.TokenRefreshException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.repositories.TokenRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.security.jwt.JwtUtils;
import vn.edu.iuh.fit.security.UserDetailsImpl;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.TokenService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.CookieUtils;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.time.Instant;
import java.util.Arrays;
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

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final TokenRepository tokenRepository;

    private final TokenService tokenService;

    private final JwtUtils jwtUtils;

    @Override
    public boolean signUpForCustomer(SignUpRequest signUpRequest) {
        UserResponse user = userService.createCustomer(signUpRequest);
        if (user == null) {
            return false;
        }
        return true;
    }

    @Override
    public SignInResponse signIn(SignInRequest signInRequest, HttpServletResponse response) {
        try {
            // Check if username exists
            String username = signInRequest.username();

            boolean existsByUsername = userService.existsByEmail(username)
                    || userService.existsByPhone(FormatPhoneNumber.normalizePhone(username));

            if (!existsByUsername) {
                throw new NotFoundException("Email or phone number does not exist. Please sign up first.");
            }

            if (!username.contains("@")) {
                username = FormatPhoneNumber.normalizePhone(username);
            }

            User findUser= userRepository.findByUsernameAndDeleteFalse(username)
                    .orElseThrow(() -> new NotFoundException("Email or phone number does not exist. Please sign up first."));

            if (!findUser.getTypeProviderAuths().contains(TypeProviderAuth.LOCAL)) {
                throw new UnauthorizedException(
                        "This account was registered with Google. Please login with Google."
                );
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.username(), signInRequest.password()));

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

            User user = userService.findById(userPrincipal.getId());

            // Update user type provider auths
            if (!user.getTypeProviderAuths().contains(TypeProviderAuth.LOCAL)) {
                user.getTypeProviderAuths().add(TypeProviderAuth.LOCAL);
                userRepository.save(user);
            }

            // Save refresh token in the database
            tokenService.saveRefreshToken(user, refreshToken);

            // Set cookie
            CookieUtils.addCookie(response, "accessToken", accessToken, jwtUtils.getTokenMaxAge(accessToken), true); // 30 minutes
            CookieUtils.addCookie(response, "refreshToken", refreshToken, jwtUtils.getTokenMaxAge(refreshToken), true); // 7 days
            // Return sign-in response
            CookieUtils.addCookie(response, "isAuthenticated", "true", jwtUtils.getTokenMaxAge(refreshToken), false);

            return SignInResponse.builder()
                    .id(userPrincipal.getId())
                    .email(userPrincipal.getEmail())
                    .phone(userPrincipal.getPhone())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Password not match. Please try again.");
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Account is disabled. Please contact support.");
        } catch (LockedException ex) {
            throw new UnauthorizedException("Account is locked. Please contact support.");
        }
    }

    @Override
    public SignInResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        // Check if cookies are present
        if (cookies == null) {
            throw new TokenRefreshException("Cookies are missing");
        }

        // Extract refresh token from cookies
        String requestRefreshToken = Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is missing"));

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
        UserDetailsImpl userDetails = UserDetailsImpl.build(user, user.getEmail() != null ? user.getEmail() : user.getPhone());
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Save the new refresh token in the database
        tokenService.saveRefreshToken(user, newRefreshToken);

        CookieUtils.addCookie(response, "accessToken", newAccessToken, jwtUtils.getTokenMaxAge(newAccessToken), true); // 30 minutes
        CookieUtils.addCookie(response, "refreshToken", newRefreshToken, jwtUtils.getTokenMaxAge(newRefreshToken), true); // 7 days

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return SignInResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        // Check if cookies are present
        if (cookies == null) {
            throw new TokenRefreshException("Cookies are missing");
        }

        // Extract refresh token from cookies
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is missing"));

        // Find the token in the database
        Token token = tokenService.findByRefreshTokenAndRevokedFalse(refreshToken);

        // Revoke the token
        tokenService.revokeToken(token);

        // Delete cookies
        CookieUtils.deleteCookie(response, "accessToken");
        CookieUtils.deleteCookie(response, "refreshToken");
        CookieUtils.deleteCookie(response, "isAuthenticated");

    }

    // Check if the token is expired
    private boolean isTokenExpired(Instant expirationDate) {
        return Instant.now().isAfter(expirationDate);
    }
}
