package com.github.starter.config;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private SecurityConfig.SecurityHeadersConfig headersConfig;
    private SecurityConfig.CorsConfig corsConfig;
    private SecurityConfig.SecretsConfig secretsConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        
        // Setup SecurityHeadersConfig
        headersConfig = new SecurityConfig.SecurityHeadersConfig();
        headersConfig.setContentSecurityPolicy("default-src 'self'");
        headersConfig.setXContentTypeOptions("nosniff");
        headersConfig.setXFrameOptions("DENY");
        headersConfig.setXXssProtection("1; mode=block");
        headersConfig.setStrictTransportSecurity("max-age=31536000; includeSubDomains");
        headersConfig.setReferrerPolicy("strict-origin-when-cross-origin");
        
        // Setup CorsConfig
        corsConfig = new SecurityConfig.CorsConfig();
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setExposedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600);
        
        // Setup SecretsConfig
        secretsConfig = new SecurityConfig.SecretsConfig();
        secretsConfig.setMaskedFields(Set.of("password", "token", "key"));
    }

    @Test
    @DisplayName("Should create security headers filter")
    void shouldCreateSecurityHeadersFilter() {
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        assertNotNull(filter);
    }

    @Test
    @DisplayName("Should configure security headers correctly")
    void shouldConfigureSecurityHeadersCorrectly() {
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        org.springframework.http.HttpHeaders headers = mock(org.springframework.http.HttpHeaders.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();
        
        verify(headers).add("Content-Security-Policy", "default-src 'self'");
        verify(headers).add("X-Content-Type-Options", "nosniff");
        verify(headers).add("X-Frame-Options", "DENY");
        verify(headers).add("X-XSS-Protection", "1; mode=block");
        verify(headers).add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        verify(headers).add("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("Should create CORS filter")
    void shouldCreateCorsFilter() {
        CorsWebFilter corsFilter = securityConfig.corsFilter(corsConfig);
        assertNotNull(corsFilter);
    }

    @Test
    @DisplayName("Should create secret masking filter")
    void shouldCreateSecretMaskingFilter() {
        SecretMaskingFilter filter = securityConfig.secretMaskingFilter(secretsConfig);
        assertNotNull(filter);
    }

    @Test
    @DisplayName("Should create configuration beans")
    void shouldCreateConfigurationBeans() {
        SecurityConfig.SecretsConfig secrets = securityConfig.secretsConfig();
        SecurityConfig.SecurityHeadersConfig headers = securityConfig.securityHeadersConfig();
        SecurityConfig.CorsConfig cors = securityConfig.corsConfig();
        
        assertNotNull(secrets);
        assertNotNull(headers);
        assertNotNull(cors);
    }

    @Test
    @DisplayName("Should handle null headers configuration gracefully")
    void shouldHandleNullHeadersConfigurationGracefully() {
        SecurityConfig.SecurityHeadersConfig nullConfig = new SecurityConfig.SecurityHeadersConfig();
        WebFilter filter = securityConfig.securityHeadersFilter(nullConfig);
        
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        org.springframework.http.HttpHeaders headers = mock(org.springframework.http.HttpHeaders.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();
        
        // Verify no headers were added when config is null
        verify(headers, never()).add(anyString(), anyString());
    }

    @Test
    @DisplayName("Should validate CORS configuration")
    void shouldValidateCorsConfiguration() {
        assertEquals(List.of("http://localhost:3000"), corsConfig.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), corsConfig.getAllowedMethods());
        assertEquals(List.of("*"), corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.isAllowCredentials());
        assertEquals(3600, corsConfig.getMaxAge());
    }

    @Test
    @DisplayName("Should validate secrets configuration")
    void shouldValidateSecretsConfiguration() {
        assertEquals(Set.of("password", "token", "key"), secretsConfig.getMaskedFields());
    }
} 