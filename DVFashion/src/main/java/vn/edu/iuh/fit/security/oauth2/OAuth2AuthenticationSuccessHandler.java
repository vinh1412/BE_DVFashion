/*
 * @ {#} OAuth2AuthenticationSuccessHandler.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.TokenRepository;
import vn.edu.iuh.fit.security.UserDetailsImpl;
import vn.edu.iuh.fit.security.jwt.JwtUtils;
import vn.edu.iuh.fit.services.TokenService;
import vn.edu.iuh.fit.services.UserService;

import java.io.IOException;

/*
 * @description: Handler for successful OAuth2 authentication
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    private final TokenService tokenService;

    private final UserService userService;

    @Value("${oauth2.redirect-uris.success}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Create UserDetailsImpl from UserPrincipal
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .loginUsername(userPrincipal.getEmail())
                .authorities(userPrincipal.getAuthorities())
                .build();

        User user = userService.findById(userPrincipal.getId());
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Save the refresh token in the database
        tokenService.saveRefreshToken(user, refreshToken);

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
    }
}
