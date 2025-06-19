package com.github.starter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Security Headers Tests")
class SecurityHeadersFilterTest {

    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private ServerHttpRequest request;
    
    @Mock
    private ServerHttpResponse response;
    
    @Mock
    private HttpHeaders responseHeaders;
    
    @Mock
    private WebFilterChain filterChain;

    private SecurityConfig securityConfig;
    private SecurityConfig.SecurityHeadersConfig headersConfig;

    @BeforeEach
    void setUp() {
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(response.getHeaders()).thenReturn(responseHeaders);
        lenient().when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        
        securityConfig = new SecurityConfig();
        headersConfig = new SecurityConfig.SecurityHeadersConfig();
    }

    @Test
    @DisplayName("Should create SecurityHeadersConfig bean")
    void shouldCreateSecurityHeadersConfigBean() {
        SecurityConfig.SecurityHeadersConfig config = securityConfig.securityHeadersConfig();
        
        assertNotNull(config);
        assertInstanceOf(SecurityConfig.SecurityHeadersConfig.class, config);
    }

    @Test
    @DisplayName("Should create security headers filter with default config")
    void shouldCreateSecurityHeadersFilterWithDefaultConfig() {
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        assertNotNull(filter);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add CSP header when configured")
    void shouldAddCSPHeaderWhenConfigured() {
        String cspValue = "default-src 'self'; script-src 'strict-dynamic'";
        headersConfig.setContentSecurityPolicy(cspValue);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Content-Security-Policy", cspValue);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add X-Content-Type-Options header when configured")
    void shouldAddXContentTypeOptionsWhenConfigured() {
        String value = "nosniff";
        headersConfig.setXContentTypeOptions(value);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("X-Content-Type-Options", value);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add X-Frame-Options header when configured")
    void shouldAddXFrameOptionsWhenConfigured() {
        String value = "DENY";
        headersConfig.setXFrameOptions(value);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("X-Frame-Options", value);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add X-XSS-Protection header when configured")
    void shouldAddXXSSProtectionWhenConfigured() {
        String value = "1; mode=block";
        headersConfig.setXXssProtection(value);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("X-XSS-Protection", value);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add Strict-Transport-Security header when configured")
    void shouldAddStrictTransportSecurityWhenConfigured() {
        String value = "max-age=31536000; includeSubDomains";
        headersConfig.setStrictTransportSecurity(value);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Strict-Transport-Security", value);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add Referrer-Policy header when configured")
    void shouldAddReferrerPolicyWhenConfigured() {
        String value = "strict-origin-when-cross-origin";
        headersConfig.setReferrerPolicy(value);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Referrer-Policy", value);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should add multiple headers when all configured")
    void shouldAddMultipleHeadersWhenAllConfigured() {
        headersConfig.setContentSecurityPolicy("default-src 'self'");
        headersConfig.setXContentTypeOptions("nosniff");
        headersConfig.setXFrameOptions("DENY");
        headersConfig.setXXssProtection("1; mode=block");
        headersConfig.setStrictTransportSecurity("max-age=31536000");
        headersConfig.setReferrerPolicy("no-referrer");
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Content-Security-Policy", "default-src 'self'");
        verify(responseHeaders).add("X-Content-Type-Options", "nosniff");
        verify(responseHeaders).add("X-Frame-Options", "DENY");
        verify(responseHeaders).add("X-XSS-Protection", "1; mode=block");
        verify(responseHeaders).add("Strict-Transport-Security", "max-age=31536000");
        verify(responseHeaders).add("Referrer-Policy", "no-referrer");
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should skip null headers")
    void shouldSkipNullHeaders() {
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders, never()).add(eq("Content-Security-Policy"), any());
        verify(responseHeaders, never()).add(eq("X-Content-Type-Options"), any());
        verify(responseHeaders, never()).add(eq("X-Frame-Options"), any());
        verify(responseHeaders, never()).add(eq("X-XSS-Protection"), any());
        verify(responseHeaders, never()).add(eq("Strict-Transport-Security"), any());
        verify(responseHeaders, never()).add(eq("Referrer-Policy"), any());
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        headersConfig.setContentSecurityPolicy("");
        headersConfig.setXFrameOptions("");
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Content-Security-Policy", "");
        verify(responseHeaders).add("X-Frame-Options", "");
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should propagate filter chain errors")
    void shouldPropagateFilterChainErrors() {
        RuntimeException error = new RuntimeException("Filter chain error");
        when(filterChain.filter(exchange)).thenReturn(Mono.error(error));
        
        headersConfig.setXContentTypeOptions("nosniff");
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectError(RuntimeException.class)
                .verify();
        
        verify(responseHeaders).add("X-Content-Type-Options", "nosniff");
    }

    @Test
    @DisplayName("Should handle special characters in header values")
    void shouldHandleSpecialCharactersInHeaderValues() {
        String complexCSP = "default-src 'self'; report-uri /csp-report?token=abc&<special>chars";
        String complexFrame = "ALLOW-FROM https://example.com/path?param=value&other=123";
        
        headersConfig.setContentSecurityPolicy(complexCSP);
        headersConfig.setXFrameOptions(complexFrame);
        
        WebFilter filter = securityConfig.securityHeadersFilter(headersConfig);
        
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
        
        verify(responseHeaders).add("Content-Security-Policy", complexCSP);
        verify(responseHeaders).add("X-Frame-Options", complexFrame);
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("SecurityHeadersConfig should have proper getters and setters")
    void shouldHaveProperGettersAndSetters() {
        String cspValue = "default-src 'self'";
        String frameValue = "SAMEORIGIN";
        String contentTypeValue = "nosniff";
        String xssValue = "1; mode=block";
        String hstsValue = "max-age=31536000";
        String referrerValue = "strict-origin";
        
        headersConfig.setContentSecurityPolicy(cspValue);
        headersConfig.setXFrameOptions(frameValue);
        headersConfig.setXContentTypeOptions(contentTypeValue);
        headersConfig.setXXssProtection(xssValue);
        headersConfig.setStrictTransportSecurity(hstsValue);
        headersConfig.setReferrerPolicy(referrerValue);
        
        assertEquals(cspValue, headersConfig.getContentSecurityPolicy());
        assertEquals(frameValue, headersConfig.getXFrameOptions());
        assertEquals(contentTypeValue, headersConfig.getXContentTypeOptions());
        assertEquals(xssValue, headersConfig.getXXssProtection());
        assertEquals(hstsValue, headersConfig.getStrictTransportSecurity());
        assertEquals(referrerValue, headersConfig.getReferrerPolicy());
    }
} 