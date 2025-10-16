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
                        .filters(f -> f.rewritePath("/auth-service/(?<segment>.*)", "/auth/${segment}"))
                        .uri("lb://auth-service"))

                // ========== Content Service Routes ==========
                .route("content-service", r -> r.path("/content-service/**")
                        .and()
                        .not(p -> p.path("/content-service/v3/api-docs/**"))
                        .filters(f -> f.rewritePath("/content-service/(?<segment>.*)", "/content/${segment}"))
                        .uri("lb://content-service"))

                // ========== Payment Service Routes ==========
                .route("payment-service", r -> r.path("/payment-service/**")
                        .and()
                        .not(p -> p.path("/payment-service/v3/api-docs/**"))
                        .filters(f -> f.rewritePath("/payment-service/(?<segment>.*)", "/payment/${segment}"))
                        .uri("lb://payment-service"))

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

                .build();
    }
}