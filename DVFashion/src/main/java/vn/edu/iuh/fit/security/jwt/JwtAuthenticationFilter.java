/*
 * @ {#} JwtAuthenticationFilter.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.security.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.Arrays;

/*
 * @description: Filter to handle JWT authentication for incoming requests
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // This method is called for every request to check if the user is authenticated
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract the JWT from the request and validate it
            String jwt = getJwtFromCookies(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // If the JWT is valid, get the username from it
                String email = jwtUtils.getUsernameFromJwtToken(jwt);

                // Load user details using the username
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                // Create an authentication token and set it in the security context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    // Parses the JWT from the Authorization header
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    // Retrieves the JWT from cookies
    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("JWT is missing in cookies"));
        }
        return null;
    }
}
