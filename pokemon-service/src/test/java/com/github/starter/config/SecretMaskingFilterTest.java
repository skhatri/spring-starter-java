package com.github.starter.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@DisplayName("SecretMaskingFilter Tests")
class SecretMaskingFilterTest {

    private SecretMaskingFilter filter;
    private ServerWebExchange exchange;
    private WebFilterChain chain;
    private ServerHttpRequest request;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        Set<String> maskedFields = Set.of("password", "token", "key");
        filter = new SecretMaskingFilter(maskedFields);
        
        exchange = mock(ServerWebExchange.class);
        chain = mock(WebFilterChain.class);
        request = mock(ServerHttpRequest.class);
        attributes = new HashMap<>();
        
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should mask sensitive query parameters")
    void shouldMaskSensitiveQueryParameters() {
        URI uri = URI.create("http://localhost:8080/api/test?password=secret&token=12345&name=test");
        when(request.getURI()).thenReturn(uri);
        
        filter.filter(exchange, chain).block();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("name=test"));
    }

    @Test
    @DisplayName("Should not modify non-sensitive query parameters")
    void shouldNotModifyNonSensitiveQueryParameters() {
        URI uri = URI.create("http://localhost:8080/api/test?name=test&age=25");
        when(request.getURI()).thenReturn(uri);
        
        filter.filter(exchange, chain).block();
        
        assertFalse(attributes.containsKey("maskedQuery"));
    }

    @Test
    @DisplayName("Should handle empty query parameters")
    void shouldHandleEmptyQueryParameters() {
        URI uri = URI.create("http://localhost:8080/api/test");
        when(request.getURI()).thenReturn(uri);
        
        filter.filter(exchange, chain).block();
        
        assertFalse(attributes.containsKey("maskedQuery"));
    }

    @Test
    @DisplayName("Should handle null values in query parameters")
    void shouldHandleNullValuesInQueryParameters() {
        URI uri = URI.create("http://localhost:8080/api/test?password=&token");
        when(request.getURI()).thenReturn(uri);
        
        filter.filter(exchange, chain).block();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
    }

    @Test
    @DisplayName("Should handle multiple values for same parameter")
    void shouldHandleMultipleValuesForSameParameter() {
        URI uri = URI.create("http://localhost:8080/api/test?password=secret1&password=secret2");
        when(request.getURI()).thenReturn(uri);
        
        filter.filter(exchange, chain).block();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
    }

    @Test
    @DisplayName("Should mask sensitive fields in query parameters")
    void shouldMaskSensitiveFieldsInQueryParameters() {
        Set<String> maskedFields = Set.of("password", "token", "key");
        SecretMaskingFilter filter = new SecretMaskingFilter(maskedFields);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        Map<String, Object> attributes = new HashMap<>();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api?password=secret&token=abc123&key=xyz789&visible=show"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("key=********"));
        assertTrue(maskedQuery.contains("visible=show"));
    }

    @Test
    @DisplayName("Should handle null masked fields")
    void shouldHandleNullMaskedFields() {
        SecretMaskingFilter filter = new SecretMaskingFilter(null);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        Map<String, Object> attributes = new HashMap<>();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api?password=secret"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertFalse(attributes.containsKey("maskedQuery"));
    }

    @Test
    @DisplayName("Should handle empty masked fields")
    void shouldHandleEmptyMaskedFields() {
        SecretMaskingFilter filter = new SecretMaskingFilter(Set.of());

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        Map<String, Object> attributes = new HashMap<>();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api?password=secret"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertFalse(attributes.containsKey("maskedQuery"));
    }

    @Test
    @DisplayName("Should handle case-insensitive field matching")
    void shouldHandleCaseInsensitiveFieldMatching() {
        Set<String> maskedFields = Set.of("PASSWORD", "Token", "KEY");
        SecretMaskingFilter filter = new SecretMaskingFilter(maskedFields);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        Map<String, Object> attributes = new HashMap<>();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api?password=secret&TOKEN=abc123&Key=xyz789"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("TOKEN=********"));
        assertTrue(maskedQuery.contains("Key=********"));
    }
} 