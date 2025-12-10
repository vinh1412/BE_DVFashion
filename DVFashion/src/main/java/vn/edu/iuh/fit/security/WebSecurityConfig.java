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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
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
                path("/auth/verify-otp-forgot-password"),
                path("/auth/verify-otp-sign-up"),
                path("/auth/reset-password-otp"),
                path("/auth/**"),
                path("/oauth2/**"),
                path("/oauth2/authorization/**"),
                path("/login/oauth2/code/**"),
                path("/recommendations/products"),
                path("/payments/paypal/success"),
                path("/payments/paypal/cancel"),
                path("/shipping/calculate"),
                path("/chat/ai"),
                "/oauth2/**",
                "/login/oauth2/**",
                "/oauth2/authorization/**",
                path("/auth/health"),
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
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        OAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/api/v1/oauth2/authorization" // Custom authorization endpoint base URI
                );
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                path("/ws-chat/**"),
                                path("/ws-chat/info"),
                                path("/ws-chat/info/**"),
                                path("/ws-chat/*/websocket"),
                                path("/ws-chat/*/xhr"),
                                path("/ws-chat/*/xhr_send"),
                                path("/ws-chat/*/xhr_streaming")
                        ).permitAll()

                        // Chat endpoints
                        .requestMatchers(
                                path("/chat/rooms"),
                                path("/chat/rooms/**"),
                                path("/chat/rooms/*/messages"),
                                path("/chat/rooms/*/messages/upload"),
                                path("/chat/rooms/*/read")
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/brands/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/categories/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/products/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/promotions/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/products/*/variants/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/product-variants/*/images/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/product-variants/*/sizes/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/cart/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/reviews/product/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/addresses/provinces/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/vouchers/customer")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/statistics/internal/revenue-timeseries")).permitAll()
                        .requestMatchers(HttpMethod.GET, path("/recommendations/today")).permitAll()
                        .requestMatchers(HttpMethod.POST, path("/cart/**")).permitAll()

                        .requestMatchers( "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()


                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                        .authorizationRequestResolver(resolver))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/v1/login/oauth2/code/*")) // Custom redirection endpoint
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
