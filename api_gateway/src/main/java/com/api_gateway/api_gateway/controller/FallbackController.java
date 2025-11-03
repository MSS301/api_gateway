package com.api_gateway.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for Circuit Breaker
 * Provides graceful degradation when downstream services are unavailable
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/auth-service")
    @PostMapping("/auth-service")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.error("Auth service is currently unavailable - Circuit Breaker OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Authentication service is temporarily unavailable. Please try again later.");
        response.put("service", "auth-service");
        response.put("circuitBreaker", "OPEN");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/content-service")
    @PostMapping("/content-service")
    public ResponseEntity<Map<String, Object>> contentServiceFallback() {
        log.error("Content service is currently unavailable - Circuit Breaker OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Content service is temporarily unavailable. Please try again later.");
        response.put("service", "content-service");
        response.put("circuitBreaker", "OPEN");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/payment-service")
    @PostMapping("/payment-service")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        log.error("Payment service is currently unavailable - Circuit Breaker OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Payment service is temporarily unavailable. Please try again later.");
        response.put("service", "payment-service");
        response.put("circuitBreaker", "OPEN");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/wallet-service")
    @PostMapping("/wallet-service")
    public ResponseEntity<Map<String, Object>> walletServiceFallback() {
        log.error("Wallet service is currently unavailable - Circuit Breaker OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Wallet service is temporarily unavailable. Please try again later.");
        response.put("service", "wallet-service");
        response.put("circuitBreaker", "OPEN");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

