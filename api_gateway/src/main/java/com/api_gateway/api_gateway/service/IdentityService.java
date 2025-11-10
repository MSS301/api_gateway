package com.api_gateway.api_gateway.service;

import com.api_gateway.api_gateway.dto.ApiResponse;
import com.api_gateway.api_gateway.dto.request.IntrospectRequest;
import com.api_gateway.api_gateway.dto.response.IntrospectResponse;
import com.api_gateway.api_gateway.repository.IdentityClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentityService {
    IdentityClient identityClient;

    /**
     * Introspect token with Circuit Breaker and Retry patterns
     * Circuit Breaker will open if auth-service is down
     * Retry will attempt 3 times with exponential backoff
     */
    @CircuitBreaker(name = "auth-service", fallbackMethod = "introspectFallback")
    @Retry(name = "auth-service")
    public Mono<ApiResponse<IntrospectResponse>> introspect(String token){
        log.debug("Calling auth-service introspect endpoint");
        return identityClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }

    /**
     * Fallback method when auth-service is unavailable
     * Returns a response indicating service is down
     */
    private Mono<ApiResponse<IntrospectResponse>> introspectFallback(String token, Exception ex) {
        log.error("Auth service is unavailable. Falling back. Error: {}", ex.getMessage());

        // Return unauthorized response when auth service is down
        IntrospectResponse response = IntrospectResponse.builder()
                .valid(false)
                .build();

        return Mono.just(ApiResponse.<IntrospectResponse>builder()
                .code(503) // Service Unavailable
                .message("Authentication service is temporarily unavailable. Please try again later.")
                .result(response)
                .build());
    }
}
