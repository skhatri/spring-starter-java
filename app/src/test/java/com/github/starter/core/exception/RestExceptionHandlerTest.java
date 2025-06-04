package com.github.starter.core.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestExceptionHandler Unit Tests")
class RestExceptionHandlerTest {

    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private ServerHttpRequest request;
    
    private RestExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
        
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(request.getPath()).thenReturn(org.springframework.http.server.RequestPath.parse("/api/test", null));
        lenient().when(request.getMethod()).thenReturn(HttpMethod.GET);
        lenient().when(request.getId()).thenReturn("test-trace-id-123");
    }

    @Nested
    @DisplayName("AppException Handling Tests")
    class AppExceptionHandlingTests {

        @Test
        @DisplayName("Should handle AppException with proper response format")
        void shouldHandleAppExceptionWithProperResponseFormat() {
            Map<String, Object> context = Map.of("field", "email", "value", "invalid");
            AppException exception = new AppException("VALIDATION_ERROR", "Invalid email format", context);
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals(400, errorDto.status());
                    assertEquals("Application Error", errorDto.error());
                    assertEquals("Invalid email format", errorDto.message());
                    assertEquals("/api/test", errorDto.path());
                    assertEquals("GET", errorDto.method());
                    assertEquals("test-trace-id-123", errorDto.traceId());
                    assertNotNull(errorDto.timestamp());
                    assertEquals("application_error", errorDto.details().get("type"));
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle AppException with null message")
        void shouldHandleAppExceptionWithNullMessage() {
            AppException exception = new AppException("ERROR_CODE", null, Map.of());
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNull(response.getBody().message());
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle AppException with empty context")
        void shouldHandleAppExceptionWithEmptyContext() {
            AppException exception = new AppException("Empty context error");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals("Empty context error", errorDto.message());
                    assertTrue(errorDto.details().containsKey("type"));
                })
                .verifyComplete();
        }

        @ParameterizedTest
        @MethodSource("appExceptionScenarios")
        @DisplayName("Should handle various AppException scenarios")
        void shouldHandleVariousAppExceptionScenarios(String errorCode, String message, Map<String, Object> context) {
            AppException exception = new AppException(errorCode, message, context);
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                })
                .verifyComplete();
        }

        static Stream<Arguments> appExceptionScenarios() {
            return Stream.of(
                Arguments.of("VALIDATION_ERROR", "Validation failed", Map.of("field", "email")),
                Arguments.of("BUSINESS_RULE_ERROR", "Business rule violated", Map.of("rule", "max_items")),
                Arguments.of("USER_ERROR", "User action required", Map.of()),
                Arguments.of("", "Empty error code", Map.of()),
                Arguments.of("UNICODE_ERROR_测试", "Unicode message 测试", Map.of("测试", "value"))
            );
        }
    }

    @Nested
    @DisplayName("DatabaseException Handling Tests")
    class DatabaseExceptionHandlingTests {

        @Test
        @DisplayName("Should handle DatabaseException with proper response format")
        void shouldHandleDatabaseExceptionWithProperResponseFormat() {
            DatabaseException exception = new DatabaseException("Connection timeout");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleDatabaseException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals(500, errorDto.status());
                    assertEquals("Database Error", errorDto.error());
                    assertEquals("A database error occurred", errorDto.message());
                    assertEquals("/api/test", errorDto.path());
                    assertEquals("GET", errorDto.method());
                    assertEquals("test-trace-id-123", errorDto.traceId());
                    assertNotNull(errorDto.timestamp());
                    assertEquals("database_error", errorDto.details().get("type"));
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should mask sensitive database error details")
        void shouldMaskSensitiveDatabaseErrorDetails() {
            DatabaseException exception = new DatabaseException("Access denied for user 'admin'@'localhost' (using password: YES)");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleDatabaseException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals("A database error occurred", errorDto.message());
                    assertNotEquals(exception.getMessage(), errorDto.message());
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle DatabaseException with null message")
        void shouldHandleDatabaseExceptionWithNullMessage() {
            DatabaseException exception = new DatabaseException((String) null);
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleDatabaseException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("A database error occurred", response.getBody().message());
                })
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException Handling Tests")
    class IllegalArgumentExceptionHandlingTests {

        @Test
        @DisplayName("Should handle IllegalArgumentException with proper response format")
        void shouldHandleIllegalArgumentExceptionWithProperResponseFormat() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter value");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleIllegalArgumentException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals(400, errorDto.status());
                    assertEquals("Invalid Request", errorDto.error());
                    assertEquals("Invalid parameter value", errorDto.message());
                    assertEquals("/api/test", errorDto.path());
                    assertEquals("GET", errorDto.method());
                    assertEquals("test-trace-id-123", errorDto.traceId());
                    assertNotNull(errorDto.timestamp());
                    assertEquals("validation_error", errorDto.details().get("type"));
                })
                .verifyComplete();
        }

        @ParameterizedTest
        @MethodSource("illegalArgumentMessages")
        @DisplayName("Should handle various IllegalArgumentException messages")
        void shouldHandleVariousIllegalArgumentExceptionMessages(String message) {
            IllegalArgumentException exception = new IllegalArgumentException(message);
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleIllegalArgumentException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(message, response.getBody().message());
                })
                .verifyComplete();
        }

        static Stream<String> illegalArgumentMessages() {
            return Stream.of(
                "Parameter cannot be null",
                "Value must be positive",
                "Invalid enum value",
                "",
                null,
                "Very long error message that contains detailed information about what went wrong in the validation process"
            );
        }
    }

    @Nested
    @DisplayName("RuntimeException Handling Tests")
    class RuntimeExceptionHandlingTests {

        @Test
        @DisplayName("Should handle generic RuntimeException with proper response format")
        void shouldHandleGenericRuntimeExceptionWithProperResponseFormat() {
            RuntimeException exception = new RuntimeException("Unexpected error occurred");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleRuntimeException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals(500, errorDto.status());
                    assertEquals("Internal Server Error", errorDto.error());
                    assertEquals("An unexpected error occurred", errorDto.message());
                    assertEquals("/api/test", errorDto.path());
                    assertEquals("GET", errorDto.method());
                    assertEquals("test-trace-id-123", errorDto.traceId());
                    assertNotNull(errorDto.timestamp());
                    assertEquals("internal_server_error", errorDto.details().get("type"));
                    assertEquals("RuntimeException", errorDto.details().get("exception_class"));
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle NullPointerException specifically")
        void shouldHandleNullPointerExceptionSpecifically() {
            NullPointerException exception = new NullPointerException("Object reference is null");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleRuntimeException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("NullPointerException", response.getBody().details().get("exception_class"));
                })
                .verifyComplete();
        }

        @ParameterizedTest
        @MethodSource("runtimeExceptionTypes")
        @DisplayName("Should handle various RuntimeException types")
        void shouldHandleVariousRuntimeExceptionTypes(RuntimeException exception, String expectedClassName) {
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleRuntimeException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals(expectedClassName, response.getBody().details().get("exception_class"));
                    assertEquals("An unexpected error occurred", response.getBody().message());
                })
                .verifyComplete();
        }

        static Stream<Arguments> runtimeExceptionTypes() {
            return Stream.of(
                Arguments.of(new RuntimeException("Test"), "RuntimeException"),
                Arguments.of(new NullPointerException("NPE"), "NullPointerException"),
                Arguments.of(new IllegalStateException("State"), "IllegalStateException"),
                Arguments.of(new UnsupportedOperationException("Op"), "UnsupportedOperationException"),
                Arguments.of(new ArrayIndexOutOfBoundsException("Index"), "ArrayIndexOutOfBoundsException")
            );
        }
    }

    @Nested
    @DisplayName("Request Context Tests")
    class RequestContextTests {

        @Test
        @DisplayName("Should extract correct request information for POST requests")
        void shouldExtractCorrectRequestInformationForPostRequests() {
            when(request.getPath()).thenReturn(org.springframework.http.server.RequestPath.parse("/api/users", null));
            when(request.getMethod()).thenReturn(HttpMethod.POST);
            when(request.getId()).thenReturn("post-trace-id");
            
            AppException exception = new AppException("Test error");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals("/api/users", errorDto.path());
                    assertEquals("POST", errorDto.method());
                    assertEquals("post-trace-id", errorDto.traceId());
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle complex paths with parameters")
        void shouldHandleComplexPathsWithParameters() {
            when(request.getPath()).thenReturn(org.springframework.http.server.RequestPath.parse("/api/users/123/orders", null));
            when(request.getMethod()).thenReturn(HttpMethod.PUT);
            
            DatabaseException exception = new DatabaseException("Update failed");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleDatabaseException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    RestExceptionHandler.ErrorDto errorDto = response.getBody();
                    assertNotNull(errorDto);
                    assertEquals("/api/users/123/orders", errorDto.path());
                    assertEquals("PUT", errorDto.method());
                })
                .verifyComplete();
        }

        @ParameterizedTest
        @MethodSource("httpMethodProvider")
        @DisplayName("Should handle all HTTP methods correctly")
        void shouldHandleAllHttpMethodsCorrectly(HttpMethod method) {
            when(request.getMethod()).thenReturn(method);
            
            RuntimeException exception = new RuntimeException("Test error");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleRuntimeException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(method.name(), response.getBody().method());
                })
                .verifyComplete();
        }

        static Stream<HttpMethod> httpMethodProvider() {
            return Stream.of(
                HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, 
                HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.HEAD, 
                HttpMethod.OPTIONS, HttpMethod.TRACE
            );
        }
    }

    @Nested
    @DisplayName("Response Timing Tests")
    class ResponseTimingTests {

        @Test
        @DisplayName("Should include recent timestamp in error response")
        void shouldIncludeRecentTimestampInErrorResponse() {
            Instant beforeCall = Instant.now();
            
            AppException exception = new AppException("Timing test error");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                handler.handleAppException(exception, exchange);
            
            StepVerifier.create(result)
                .assertNext(response -> {
                    Instant afterCall = Instant.now();
                    Instant responseTimestamp = response.getBody().timestamp();
                    
                    assertTrue(responseTimestamp.isAfter(beforeCall.minusSeconds(1)));
                    assertTrue(responseTimestamp.isBefore(afterCall.plusSeconds(1)));
                })
                .verifyComplete();
        }

        @Test
        @DisplayName("Should have consistent timestamp format across different exceptions")
        void shouldHaveConsistentTimestampFormatAcrossDifferentExceptions() {
            AppException appEx = new AppException("App error");
            DatabaseException dbEx = new DatabaseException("DB error");
            RuntimeException rtEx = new RuntimeException("Runtime error");
            
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> appResult = 
                handler.handleAppException(appEx, exchange);
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> dbResult = 
                handler.handleDatabaseException(dbEx, exchange);
            Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> rtResult = 
                handler.handleRuntimeException(rtEx, exchange);
            
            StepVerifier.create(Mono.zip(appResult, dbResult, rtResult))
                .assertNext(tuple -> {
                    Instant appTimestamp = tuple.getT1().getBody().timestamp();
                    Instant dbTimestamp = tuple.getT2().getBody().timestamp();
                    Instant rtTimestamp = tuple.getT3().getBody().timestamp();
                    
                    assertNotNull(appTimestamp);
                    assertNotNull(dbTimestamp);
                    assertNotNull(rtTimestamp);
                    
                    assertTrue(Math.abs(appTimestamp.toEpochMilli() - dbTimestamp.toEpochMilli()) < 1000);
                    assertTrue(Math.abs(dbTimestamp.toEpochMilli() - rtTimestamp.toEpochMilli()) < 1000);
                })
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle exceptions efficiently under load")
        void shouldHandleExceptionsEfficientlyUnderLoad() {
            AppException exception = new AppException("Performance test error");
            
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 1000; i++) {
                Mono<ResponseEntity<RestExceptionHandler.ErrorDto>> result = 
                    handler.handleAppException(exception, exchange);
                
                result.block();
            }
            
            long duration = System.nanoTime() - startTime;
            long durationMs = duration / 1_000_000;
            
            boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
            long threshold = isCI ? 5000 : 500;
            
            assertTrue(durationMs < threshold, 
                "Handling 1000 exceptions should take under " + threshold + "ms, took: " + durationMs + "ms");
        }
    }
} 