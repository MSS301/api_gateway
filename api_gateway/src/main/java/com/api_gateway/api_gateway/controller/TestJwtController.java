package com.api_gateway.api_gateway.controller;

import com.api_gateway.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for demonstrating JWT functionality.
 * 
 * This controller is for TESTING purposes only and should be removed
 * or secured in production environments.
 * 
 * In production, token generation should be handled by the auth-service,
 * not the API Gateway.
 * 
 * Endpoints:
 * - POST /api/test/token - Generate a test JWT token
 * - GET /api/test/validate - Validate a JWT token
 * - GET /api/test/extract - Extract user ID from token
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestJwtController {

    private final JwtUtil jwtUtil;

    /**
     * Generate a test JWT token.
     * 
     * POST /api/test/token
     * Body: {
     *   "userId": "user123",
     *   "scope": "ROLE_USER",
     *   "expirationSeconds": 3600
     * }
     * 
     * Response: {
     *   "token": "eyJhbGc...",
     *   "userId": "user123",
     *   "expiresIn": 3600
     * }
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String scope = (String) request.getOrDefault("scope", "ROLE_USER");
        Long expirationSeconds = request.containsKey("expirationSeconds") 
            ? Long.parseLong(request.get("expirationSeconds").toString())
            : 3600L;

        String token = jwtUtil.generateToken(userId, scope, expirationSeconds);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        response.put("expiresIn", expirationSeconds);
        response.put("note", "Use this token in Authorization header: Bearer " + token);

        log.info("Generated test token for user: {}", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate a JWT token.
     * 
     * GET /api/test/validate
     * Header: Authorization: Bearer <token>
     * 
     * Response: {
     *   "valid": true,
     *   "message": "Token is valid"
     * }
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Missing or invalid Authorization header");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);
        boolean valid = jwtUtil.validateToken(token);

        response.put("valid", valid);
        response.put("message", valid ? "Token is valid" : "Token is invalid or expired");

        if (valid) {
            String userId = jwtUtil.extractUserId(token);
            response.put("userId", userId);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Extract user ID from JWT token.
     * 
     * GET /api/test/extract
     * Header: Authorization: Bearer <token>
     * 
     * Response: {
     *   "userId": "user123",
     *   "extracted": true
     * }
     */
    @GetMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractUserId(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("extracted", false);
            response.put("message", "Missing or invalid Authorization header");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token);

        if (userId != null) {
            response.put("extracted", true);
            response.put("userId", userId);
            response.put("message", "User ID extracted successfully");
        } else {
            response.put("extracted", false);
            response.put("message", "Failed to extract user ID from token");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to verify the gateway injects X-User-Id header.
     * 
     * GET /api/test/headers
     * Header: Authorization: Bearer <token>
     * 
     * Response: All request headers including X-User-Id
     */
    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> getHeaders(
            @RequestHeader Map<String, String> headers) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("headers", headers);
        response.put("hasXUserId", headers.containsKey("x-user-id"));
        
        if (headers.containsKey("x-user-id")) {
            response.put("X-User-Id", headers.get("x-user-id"));
            response.put("message", "X-User-Id header was successfully injected by gateway");
        } else {
            response.put("message", "X-User-Id header not found (check if authentication succeeded)");
        }

        return ResponseEntity.ok(response);
    }
}
