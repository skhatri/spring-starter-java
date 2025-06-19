package com.github.starter.config;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecretMaskingFilter Advanced Tests")
class SecretMaskingFilterAdvancedTest {

    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private ServerHttpRequest request;
    
    @Mock
    private WebFilterChain filterChain;

    private SecretMaskingFilter filter;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        Set<String> maskedFields = Set.of("password", "token", "key", "secret", "apikey");
        filter = new SecretMaskingFilter(maskedFields);
        attributes = new HashMap<>();
        
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getAttributes()).thenReturn(attributes);
        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should handle large query parameter sets efficiently")
    void shouldHandleLargeQueryParameterSetsEfficiently() {
        StringBuilder queryBuilder = new StringBuilder("http://localhost:8080/api/test?");
        
        for (int i = 0; i < 1000; i++) {
            queryBuilder.append("param").append(i).append("=value").append(i).append("&");
        }
        queryBuilder.append("password=secret&token=12345");
        
        URI uri = URI.create(queryBuilder.toString());
        when(request.getURI()).thenReturn(uri);
        
        long startTime = System.nanoTime();
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        long duration = System.nanoTime() - startTime;
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        
        assertTrue(duration < Duration.ofMillis(100).toNanos(), 
            "Processing should complete in under 100ms, took: " + Duration.ofNanos(duration).toMillis() + "ms");
    }

    @Test
    @DisplayName("Should handle extremely long parameter values")
    void shouldHandleExtremelyLongParameterValues() {
        String longValue = "x".repeat(10240);
        String longSecretValue = "secret".repeat(2048);
        String query = "http://localhost:8080/api/test?normalParam=" + longValue + 
                      "&password=" + longSecretValue + "&token=abc123";
        
        URI uri = URI.create(query);
        when(request.getURI()).thenReturn(uri);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("normalParam=" + longValue));
    }

    @Test
    @DisplayName("Should handle basic special characters")
    void shouldHandleBasicSpecialCharacters() {
        String query = "http://localhost:8080/api/test?" +
                "password=secret123&token=abc456&normal=test&" +
                "key=xyz789&special=value_with-special.chars";
        
        URI uri = URI.create(query);
        when(request.getURI()).thenReturn(uri);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("key=********"));
        assertTrue(maskedQuery.contains("normal=test"));
        assertTrue(maskedQuery.contains("special=value_with-special.chars"));
    }

    @Test
    @DisplayName("Should handle malformed query parameters gracefully")
    void shouldHandleMalformedQueryParametersGracefully() {
        String malformedQuery = "http://localhost:8080/api/test?" +
                "password=secret&=missingname&=&password&token=value&=another&" +
                "key===multiple=equals&normal=value&&&empty=";
        
        URI uri = URI.create(malformedQuery);
        when(request.getURI()).thenReturn(uri);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("key=********"));
    }

    @Test
    @DisplayName("Should perform efficiently with high-frequency regex matching")
    void shouldPerformEfficientlyWithHighFrequencyRegexMatching() {
        String query = "http://localhost:8080/api/test?" +
                "password=secret1&token=token1&key=key1&password=secret2&" +
                "token=token2&key=key2&password=secret3&token=token3&key=key3";
        
        URI uri = URI.create(query);
        when(request.getURI()).thenReturn(uri);
        
        long totalTime = 0;
        int iterations = 1000;
        
        for (int i = 0; i < iterations; i++) {
            attributes.clear();
            long startTime = System.nanoTime();
            filter.filter(exchange, filterChain).block();
            totalTime += (System.nanoTime() - startTime);
        }
        
        long averageTimeNs = totalTime / iterations;
        long averageTimeMs = averageTimeNs / 1_000_000;
        
        boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
        long threshold = isCI ? 100 : 10;
        
        assertTrue(averageTimeMs < threshold, 
            "Average processing time should be under " + threshold + "ms, was: " + averageTimeMs + "ms");
    }

    @Test
    @DisplayName("Should be thread-safe with pattern compilation")
    void shouldBeThreadSafeWithPatternCompilation() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            @SuppressWarnings({"FutureReturnValueIgnored", "unused"})
            var unused = executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    Set<String> threadSpecificFields = Set.of("password" + threadId, "token" + threadId);
                    SecretMaskingFilter threadFilter = new SecretMaskingFilter(threadSpecificFields);
                    
                    assertNotNull(threadFilter, "Filter should be created successfully");
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        assertTrue(completionLatch.await(10, TimeUnit.SECONDS));
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        assertEquals(threadCount, successCount.get(), 
            "All threads should successfully create filters");
    }

    @Test
    @DisplayName("Should validate memory usage with moderate stress testing")
    void shouldValidateMemoryUsageWithModerateStressTesting() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> testAttributes = new HashMap<>();
            when(exchange.getAttributes()).thenReturn(testAttributes);
            
            String query = "http://localhost:8080/api/test?" +
                    "password=secretvalue" + i + "&token=tokenvalue" + i + 
                    "&key=keyvalue" + i + "&data=datavalue" + i;
            
            when(request.getURI()).thenReturn(URI.create(query));
            filter.filter(exchange, filterChain).block();
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        long maxMemoryMB = 20;
        assertTrue(memoryUsed < maxMemoryMB * 1024 * 1024, 
            "Memory usage should be under " + maxMemoryMB + "MB, used: " + 
            (memoryUsed / 1024 / 1024) + "MB");
    }

    @Test
    @DisplayName("Should maintain filter state isolation between requests")
    void shouldMaintainFilterStateIsolationBetweenRequests() {
        Map<String, Object> attributes1 = new HashMap<>();
        Map<String, Object> attributes2 = new HashMap<>();
        
        when(exchange.getAttributes()).thenReturn(attributes1);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test?password=secret1"));
        filter.filter(exchange, filterChain).block();
        String result1 = (String) attributes1.get("maskedQuery");
        
        when(exchange.getAttributes()).thenReturn(attributes2);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test?token=secret2"));
        filter.filter(exchange, filterChain).block();
        String result2 = (String) attributes2.get("maskedQuery");
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("password=********"));
        assertTrue(result2.contains("token=********"));
        assertNotEquals(result1, result2);
    }

    @Test
    @DisplayName("Should handle empty and whitespace parameter values")
    void shouldHandleEmptyAndWhitespaceParameterValues() {
        String query = "http://localhost:8080/api/test?" +
                "password=&token=%20&key=+++&secret=%09%0A%0D&normal=value";
        
        URI uri = URI.create(query);
        when(request.getURI()).thenReturn(uri);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("key=********"));
        assertTrue(maskedQuery.contains("secret=********"));
        assertTrue(maskedQuery.contains("normal=value"));
    }

    @Test
    @DisplayName("Should handle duplicate sensitive parameters correctly")
    void shouldHandleDuplicateSensitiveParametersCorrectly() {
        String query = "http://localhost:8080/api/test?" +
                "password=first&other=value&password=second&token=abc&password=third";
        
        URI uri = URI.create(query);
        when(request.getURI()).thenReturn(uri);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        
        long passwordMaskCount = (long) maskedQuery.chars()
                .mapToObj(c -> (char) c)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString()
                .split("password=\\*{8}", -1).length - 1L;
        
        assertEquals(3L, passwordMaskCount, "All three password parameters should be masked");
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("other=value"));
    }

    @Test
    @DisplayName("Should handle regex performance benchmarks")
    void shouldHandleRegexPerformanceBenchmarks() {
        String complexQuery = "http://localhost:8080/api/test?" +
                "password=secret&token=abc123&key=xyz789&" +
                "password1=test&password2=demo&token1=value&token2=another&" +
                "normalParam1=value1&normalParam2=value2&normalParam3=value3";
        
        URI uri = URI.create(complexQuery);
        when(request.getURI()).thenReturn(uri);
        
        long totalTime = 0;
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            attributes.clear();
            long startTime = System.nanoTime();
            filter.filter(exchange, filterChain).block();
            totalTime += (System.nanoTime() - startTime);
        }
        
        long averageTime = totalTime / iterations;
        assertTrue(averageTime < Duration.ofMillis(10).toNanos(),
            "Average processing time should be under 10ms for complex queries");
        
        assertTrue(attributes.containsKey("maskedQuery"));
        String maskedQuery = (String) attributes.get("maskedQuery");
        assertTrue(maskedQuery.contains("password=********"));
        assertTrue(maskedQuery.contains("token=********"));
        assertTrue(maskedQuery.contains("key=********"));
    }

    @Test
    @DisplayName("Should validate filter creation with different field sets")
    void shouldValidateFilterCreationWithDifferentFieldSets() {
        SecretMaskingFilter nullFilter = new SecretMaskingFilter(null);
        SecretMaskingFilter emptyFilter = new SecretMaskingFilter(Set.of());
        SecretMaskingFilter normalFilter = new SecretMaskingFilter(Set.of("password", "token"));
        
        assertNotNull(nullFilter, "Filter with null fields should be created");
        assertNotNull(emptyFilter, "Filter with empty fields should be created");
        assertNotNull(normalFilter, "Filter with normal fields should be created");
    }

    @Test
    @DisplayName("Should demonstrate pattern compilation efficiency")
    void shouldDemonstratePatternCompilationEfficiency() {
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 100; i++) {
            new SecretMaskingFilter(Set.of("password", "token", "key", "secret"));
        }
        
        long duration = System.nanoTime() - startTime;
        long durationMs = duration / 1_000_000;
        
        boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
        long threshold = isCI ? 1000 : 100;
        
        assertTrue(durationMs < threshold, 
            "Creating 100 filters should take under " + threshold + "ms, took: " + durationMs + "ms");
    }
} 