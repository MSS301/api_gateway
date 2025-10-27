package com.api_gateway.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Global filter that extracts user_id from validated JWT token and injects it
 * as X-User-Id header before forwarding requests to downstream services.
 * 
 * This filter runs AFTER authentication (higher order number means later execution).
 * It assumes the JWT has already been validated by JwtAuthenticationManager.
 * 
 * Flow:
 * 1. Extract JWT token from Authorization header
 * 2. Parse the token to get claims (token is already validated)
 * 3. Extract user_id from the 'sub' claim (or 'user_id' claim as fallback)
 * 4. Inject X-User-Id header into the request
 * 5. Forward the modified request to downstream service
 * 
 * Downstream services can trust the X-User-Id header since it comes from
 * the gateway after JWT validation.
 */
@Component
@Slf4j
public class JwtUserIdInjectionFilter implements GlobalFilter, Ordered {

    // Header name that will be injected with user ID
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Filter execution order. Higher value = later execution.
     * We want this to run AFTER authentication filters (which typically run at order 0).
     * Setting to 1 ensures JWT is already validated before we extract user_id.
     */
    @Override
    public int getOrder() {
        return 1; // Run after authentication
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If no Authorization header or not a Bearer token, continue without modification
        // (public endpoints or already failed authentication)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Bearer token found, skipping user ID injection for path: {}", 
                request.getPath());
            return chain.filter(exchange);
        }

        try {
            // Extract token (remove "Bearer " prefix)
            String token = authHeader.substring(BEARER_PREFIX.length());

            // Parse JWT to extract claims
            // Note: Token is already validated by JwtAuthenticationManager,
            // but we need to parse it again to extract the user_id claim
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract user ID from token
            // Try 'sub' claim first (standard JWT subject), then 'user_id' as fallback
            String userId = claims.getSubject();
            if (userId == null || userId.isEmpty()) {
                // Fallback to 'user_id' claim if 'sub' is not present
                userId = claims.get("user_id", String.class);
            }

            if (userId != null && !userId.isEmpty()) {
                // Create a modified request with the X-User-Id header
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(USER_ID_HEADER, userId)
                        .build();

                log.debug("Injected header {}={} for path: {}", 
                    USER_ID_HEADER, userId, request.getPath());

                // Continue with the modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } else {
                log.warn("Could not extract user_id from JWT for path: {}", request.getPath());
                // Continue without modification
                return chain.filter(exchange);
            }

        } catch (Exception e) {
            // If token parsing fails here, it likely already failed in authentication
            // Log the error but don't block the request (authentication will handle rejection)
            log.error("Error extracting user_id from JWT for path: {} - {}", 
                request.getPath(), e.getMessage());
            return chain.filter(exchange);
        }
    }
}
