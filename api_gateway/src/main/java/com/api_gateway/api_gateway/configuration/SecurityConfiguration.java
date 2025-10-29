package com.api_gateway.api_gateway.configuration;

import com.api_gateway.api_gateway.exception.JwtAuthenticationEntryPoint;
import com.api_gateway.api_gateway.security.JwtAuthenticationManager;
import com.api_gateway.api_gateway.security.JwtServerAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtServerAuthenticationConverter jwtServerAuthenticationConverter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] PUBLIC_ENDPOINTS = {
            // Auth Service - Public endpoints
            "/auth-service/hello",
            "/auth-service/auth/token",
            "/auth-service/auth/introspect",
            "/auth-service/auth/refresh",
            "/auth-service/users", // POST only (registration)
            "/auth-service/auth/email-verification",
            "/auth-service/auth/resend-verification",
            "/auth-service/auth/google",
            "/auth-service/login/oauth2/code/google",
            // OAuth2 endpoints - MUST be before any authenticated patterns
            "/auth-service/login/**",
            "/auth-service/oauth2/**",
            "/auth-service/auth/google/**",

            // Direct auth service access (when bypassing gateway)
            "/login/**",
            "/oauth2/**",

            // Payment Service - Webhooks (must be public for external services)
            "/payment-service/payment/webhook",
            "/payment-service/payment/return",
            "/payment-service/payment/cancel",

            // API Documentation - Public access
            "/*/v3/api-docs/**",
            "/*/swagger-ui/**",
            "/*/swagger-ui.html",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",

            // Health checks
            "/*/actuator/health",
            "/actuator/**",

            // Favicon
            "/favicon.ico"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Create authentication filter
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Add JWT authentication filter
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(exchanges -> exchanges
                        // Allow CORS preflight requests through security
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ========== PUBLIC ENDPOINTS ==========
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // ========== ADMIN ENDPOINTS ==========
                        // Auth Service - Admin only
                        .pathMatchers("/auth-service/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/auth-service/roles/**").hasRole("ADMIN")
                        .pathMatchers("/auth-service/permissions/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/auth-service/users").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/auth-service/users/**").hasRole("ADMIN")

                        // Wallet Service - Admin only
                        .pathMatchers("/wallet-service/api/admin/**").hasRole("ADMIN")

                        // Content Service - Admin only
                        .pathMatchers("/content-service/admin/**").hasRole("ADMIN")

                        // ========== PAYMENT SERVICE ==========
                        .pathMatchers(HttpMethod.POST, "/payment-service/payment").authenticated()
                        .pathMatchers(HttpMethod.GET, "/payment-service/payment/**").authenticated()

                        // ========== WALLET SERVICE ==========
                        .pathMatchers("/wallet-service/api/wallets/my/**").authenticated()
                        .pathMatchers("/wallet-service/internal/**").permitAll() // Service-to-service

                        // ========== CONTENT SERVICE ==========
                        // Public browsing
                        .pathMatchers(HttpMethod.GET, "/content-service/subjects/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/content-service/chapters/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/content-service/curriculum-lessons/**").permitAll()

                        // Authenticated operations
                        .pathMatchers(HttpMethod.POST, "/content-service/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/content-service/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/content-service/**").authenticated()

                        // ========== AUTH SERVICE ==========
                        .pathMatchers(HttpMethod.GET, "/auth-service/users/**").authenticated()

                        // ========== DEFAULT ==========
                        .anyExchange().authenticated()
                )
                .build();
    }
}