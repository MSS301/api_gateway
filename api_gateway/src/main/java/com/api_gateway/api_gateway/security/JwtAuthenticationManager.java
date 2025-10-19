package com.api_gateway.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .handle((auth, sink) -> {
                    String token = auth.getCredentials().toString();
                    try {
                        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                        Claims claims = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                        String userId = claims.getSubject();
                        String scope = claims.get("scope", String.class);

                        Collection<? extends GrantedAuthority> authorities = Arrays.stream(scope.split(" "))
                                .filter(s -> !s.isEmpty())
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        log.debug("Authenticated user: {}, authorities: {}", userId, authorities);

                        sink.next(new UsernamePasswordAuthenticationToken(userId, token, authorities));
                    } catch (Exception e) {
                        log.error("JWT validation failed: {}", e.getMessage());
                        sink.error(new RuntimeException("Invalid token"));
                    }
                });
    }
}
