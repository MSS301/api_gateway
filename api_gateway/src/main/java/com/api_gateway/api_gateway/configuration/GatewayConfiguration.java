package com.api_gateway.api_gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ========== Auth Service Routes ==========
                .route("auth-service", r -> r.path("/auth-service/**")
                        .and()
                        .not(p -> p.path("/auth-service/v3/api-docs/**"))
                        .filters(f -> f
                                .rewritePath("/auth-service/(?<segment>.*)", "/auth/${segment}"))
                        .uri("lb://auth-service"))

                // ========== Content Service Routes ==========
                .route("content-service", r -> r.path("/content-service/**")
                        .and()
                        .not(p -> p.path("/content-service/v3/api-docs/**"))
                        .filters(f -> f
                                .rewritePath("/content-service/(?<segment>.*)", "/content/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("content-service")
                                        .setFallbackUri("forward:/fallback/content-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("lb://content-service"))

                // ========== Payment Service Routes ==========
                // Direct /payment/** routes (for webhooks and callbacks)
                .route("payment-direct", r -> r.path("/payment/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .circuitBreaker(c -> c
                                        .setName("payment-service")
                                        .setFallbackUri("forward:/fallback/payment-service")))
                        .uri("lb://payment-service"))
                
                // /payment-service/** routes (standard API calls)
                .route("payment-service", r -> r.path("/payment-service/**")
                        .and()
                        .not(p -> p.path("/payment-service/v3/api-docs/**"))
                        .filters(f -> f
                                .rewritePath("/payment-service/(?<segment>.*)", "/payment/${segment}")
                                .preserveHostHeader()
                                .circuitBreaker(c -> c
                                        .setName("payment-service")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("lb://payment-service"))

                // ========== Wallet Service Routes ==========
                .route("wallet-service", r -> r.path("/wallet-service/**")
                        .and()
                        .not(p -> p.path("/wallet-service/v3/api-docs/**"))
                        .filters(f -> f
                                .rewritePath("/wallet-service/(?<segment>.*)", "/wallet/${segment}")
                                .preserveHostHeader()
                                .circuitBreaker(c -> c
                                        .setName("wallet-service")
                                        .setFallbackUri("forward:/fallback/wallet-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("lb://wallet-service"))

                // ========== OpenAPI Documentation Routes ==========

                // Auth Service OpenAPI
                .route("auth-service-openapi", r -> r.path("/auth-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/auth-service/(?<segment>.*)", "/auth/${segment}"))
                        .uri("lb://auth-service"))

                // Content Service OpenAPI
                .route("content-service-openapi", r -> r.path("/content-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/content-service/(?<segment>.*)", "/content/${segment}"))
                        .uri("lb://content-service"))

                // Payment Service OpenAPI
                .route("payment-service-openapi", r -> r.path("/payment-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/payment-service/(?<segment>.*)", "/payment/${segment}"))
                        .uri("lb://payment-service"))
                // ========== Mindmap Service Routes ==========
                .route("mindmap-service", r -> r.path("/mindmap-service/**")
                        .and()
                        .not(p -> p.path("/mindmap-service/v3/api-docs/**"))
                        .filters(f -> f
                                .rewritePath("/mindmap-service/(?<segment>.*)", "/mindmap-service/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("mindmap-service")
                                        .setFallbackUri("forward:/fallback/mindmap-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("lb://mindmap-service"))

                // Mindmap Service OpenAPI
                .route("mindmap-service-openapi", r -> r.path("/mindmap-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/mindmap-service/(?<segment>.*)", "/mindmap-service/${segment}"))
                        .uri("lb://mindmap-service"))
                // Wallet Service OpenAPI
                .route("wallet-service-openapi", r -> r.path("/wallet-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/wallet-service/(?<segment>.*)", "/wallet/${segment}"))
                        .uri("lb://wallet-service"))

                // ========== AI Service Chatbot Routes ==========
                // AI Service Chatbot (Python FastAPI on port 8000)
                // Route 1: /ai-service/** (legacy)
                .route("ai-service", r -> r.path("/ai-service/**")
                        .and()
                        .not(p -> p.path("/ai-service/docs/**"))
                        .and()
                        .not(p -> p.path("/ai-service/openapi.json"))
                        .filters(f -> f
                                .rewritePath("/ai-service/(?<segment>.*)", "/${segment}")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*")
                                .setResponseHeader("Access-Control-Expose-Headers", "*")
                                .circuitBreaker(c -> c
                                        .setName("ai-service")
                                        .setFallbackUri("forward:/fallback/ai-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("http://localhost:8000"))
                
                // Route 2: /ai-chatbot-service/** (new naming convention)
                .route("ai-chatbot-service", r -> r.path("/ai-chatbot-service/**")
                        .and()
                        .not(p -> p.path("/ai-chatbot-service/docs/**"))
                        .and()
                        .not(p -> p.path("/ai-chatbot-service/openapi.json"))
                        .filters(f -> f
                                .rewritePath("/ai-chatbot-service/(?<segment>.*)", "/${segment}")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*")
                                .setResponseHeader("Access-Control-Expose-Headers", "*")
                                .circuitBreaker(c -> c
                                        .setName("ai-service")
                                        .setFallbackUri("forward:/fallback/ai-service"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri("http://localhost:8000"))

                // AI Service OpenAPI docs proxy (to avoid CORS issues)
                .route("ai-service-docs", r -> r.path("/ai-service/docs/**")
                        .filters(f -> f
                                .rewritePath("/ai-service/docs/(?<segment>.*)", "/docs/${segment}")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*"))
                        .uri("http://localhost:8000"))

                // AI Service OpenAPI JSON proxy
                .route("ai-service-openapi", r -> r.path("/ai-service/openapi.json")
                        .filters(f -> f
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*")
                                .rewritePath("/ai-service/openapi.json", "/openapi.json"))
                        .uri("http://localhost:8000"))
                
                // AI Chatbot Service OpenAPI docs proxy
                .route("ai-chatbot-service-docs", r -> r.path("/ai-chatbot-service/docs/**")
                        .filters(f -> f
                                .rewritePath("/ai-chatbot-service/docs/(?<segment>.*)", "/docs/${segment}")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*"))
                        .uri("http://localhost:8000"))

                // AI Chatbot Service OpenAPI JSON proxy
                .route("ai-chatbot-service-openapi", r -> r.path("/ai-chatbot-service/openapi.json")
                        .filters(f -> f
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*")
                                .rewritePath("/ai-chatbot-service/openapi.json", "/openapi.json"))
                        .uri("http://localhost:8000"))

                .build();
    }
}
