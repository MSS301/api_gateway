package com.api_gateway.api_gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom authentication entry point that returns JSON error responses
 * for authentication failures (invalid or missing JWT tokens).
 * 
 * This replaces the default Spring Security behavior that returns
 * WWW-Authenticate headers causing browser login prompts.
 * 
 * Returns HTTP 401 with JSON body:
 * {
 *   "timestamp": "2025-10-27T22:30:00",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Invalid or expired JWT token",
 *   "path": "/payment-service/payment/123"
 * }
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Invalid or expired JWT token");
        errorResponse.put("path", exchange.getRequest().getPath().value());

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            log.warn("Authentication failed for path: {} - {}", 
                exchange.getRequest().getPath(), ex.getMessage());

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing authentication error response", e);
            return response.setComplete();
        }
    }
}
