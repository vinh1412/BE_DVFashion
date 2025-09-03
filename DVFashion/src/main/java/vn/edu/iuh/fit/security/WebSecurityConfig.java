/*
 * @ {#} WebSecurityConfig.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.iuh.fit.security.jwt.JwtAuthenticationFilter;
import vn.edu.iuh.fit.security.oauth2.CustomOAuth2UserService;
import vn.edu.iuh.fit.security.oauth2.OAuth2AuthenticationFailureHandler;
import vn.edu.iuh.fit.security.oauth2.OAuth2AuthenticationSuccessHandler;

/*
 * @description: Configuration class for Spring Security
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    private final AuthEntryPointJwt unauthorizedHandler;

    private final PasswordEncoder passwordEncoder;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Value("${web.base-path}")
    private String basePath;

    private String[] PUBLIC_ENDPOINTS;

    private String path(String subPath) {
        return basePath + subPath;
    }

    @PostConstruct
    public void initPublicEndpoints() {
        PUBLIC_ENDPOINTS = new String[]{
                path("/auth/sign-up"),
                path("/auth/sign-in"),
                path("/auth/refresh-token"),
                path("/auth/forgot-password"),
                path("/auth/password/*"),
                path("/auth/reset-password-mail"),
                path("/auth/verify-otp"),
                path("/auth/reset-password-otp"),
                path("/auth/**"),
                path("/oauth2/**"),
                "/oauth2/**",
                "/login/oauth2/**",
                "/oauth2/authorization/**",
        };
    }

    @Bean
    public JwtAuthenticationFilter authenticationJwtTokenFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, path("/brands/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/categories/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/products/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/products/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/products/*/variants/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/product-variants/*/images/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/product-variants/*/sizes/**")).permitAll()

                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
//                        .authorizationEndpoint(authorization -> authorization
//                                .baseUri("/oauth2/authorize"))
//                        .redirectionEndpoint(redirection -> redirection
//                                .baseUri("/oauth2/callback/*"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        // Use custom authentication provider
        http.authenticationProvider(authenticationProvider());
        // Add JWT authentication filter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
