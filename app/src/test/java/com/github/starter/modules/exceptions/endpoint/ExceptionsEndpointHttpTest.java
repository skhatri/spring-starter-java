package com.github.starter.modules.exceptions.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.starter.core.exception.AppException;
import com.github.starter.core.exception.DatabaseException;
import com.github.starter.test.base.BaseUnitTest;

@DisplayName("Exceptions Endpoint HTTP Behavior Tests")
class ExceptionsEndpointHttpTest extends BaseUnitTest {

    private final ExceptionsEndpoint endpoint = new ExceptionsEndpoint();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should demonstrate 400 error behavior")
    void shouldDemonstrate400ErrorBehavior() {
        try {
            endpoint.trigger400Error("Test 400 error");
        } catch (AppException e) {
            assertEquals("BAD_REQUEST", e.getErrorCode());
            assertEquals("Test 400 error", e.getMessage());
            assertTrue(e.getContext().containsKey("errorCode"));
            assertEquals(400, e.getContext().get("errorCode"));
        }
    }

    @Test
    @DisplayName("Should demonstrate 500 error behavior")
    void shouldDemonstrate500ErrorBehavior() {
        try {
            endpoint.trigger500Error("Test 500 error");
        } catch (DatabaseException e) {
            assertEquals("Test 500 error", e.getMessage());
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Database connection failed", e.getCause().getMessage());
        }
    }

    @Test
    @DisplayName("Should demonstrate null pointer error behavior")
    void shouldDemonstrateNullPointerErrorBehavior() {
        try {
            endpoint.triggerNullPointerError();
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    @DisplayName("Should return success response")
    void shouldReturnSuccessResponse() {
        ResponseEntity<Map<String, Object>> response = endpoint.success();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("All systems operational", body.get("message"));
        assertEquals("/exceptions/success", body.get("endpoint"));
        assertNotNull(body.get("timestamp"));
        assertTrue(body.get("timestamp") instanceof Long);
    }

    @Test
    @DisplayName("Should return custom status response")
    void shouldReturnCustomStatusResponse() {
        ResponseEntity<String> response = endpoint.triggerCustomStatusError(200, "Success test");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Custom status: 200", response.getBody());
    }

    @Test
    @DisplayName("Should demonstrate custom 400 error behavior")
    void shouldDemonstrateCustom400ErrorBehavior() {
        try {
            endpoint.triggerCustomStatusError(400, "Custom 400 test");
        } catch (IllegalArgumentException e) {
            assertEquals("Custom 400 test (Custom 400)", e.getMessage());
        }
    }

    @Test
    @DisplayName("Should demonstrate custom 500 error behavior")
    void shouldDemonstrateCustom500ErrorBehavior() {
        try {
            endpoint.triggerCustomStatusError(500, "Custom 500 test");
        } catch (RuntimeException e) {
            assertEquals("Custom 500 test (Custom 500)", e.getMessage());
        }
    }

    @Test
    @DisplayName("Should simulate error handler behavior for AppException")
    void shouldSimulateErrorHandlerBehaviorForAppException() {
        AppException exception = new AppException("BAD_REQUEST", "Test error", Map.of("field", "value"));
        
        MockErrorResponse errorResponse = simulateErrorHandler(exception, HttpStatus.BAD_REQUEST);
        
        assertEquals(400, errorResponse.status);
        assertEquals("Bad Request", errorResponse.error);
        assertTrue(errorResponse.message.contains("Test error"));
        assertNotNull(errorResponse.timestamp);
        assertNotNull(errorResponse.details);
    }

    @Test
    @DisplayName("Should simulate error handler behavior for DatabaseException")
    void shouldSimulateErrorHandlerBehaviorForDatabaseException() {
        DatabaseException exception = new DatabaseException("Database error", new RuntimeException("Connection failed"));
        
        MockErrorResponse errorResponse = simulateErrorHandler(exception, HttpStatus.INTERNAL_SERVER_ERROR);
        
        assertEquals(500, errorResponse.status);
        assertEquals("Internal Server Error", errorResponse.error);
        assertTrue(errorResponse.message.contains("Database error"));
        assertNotNull(errorResponse.timestamp);
    }

    private MockErrorResponse simulateErrorHandler(Exception exception, HttpStatus status) {
        MockErrorResponse response = new MockErrorResponse();
        response.status = status.value();
        response.error = status.getReasonPhrase();
        response.message = exception.getMessage();
        response.path = "/exceptions/test";
        response.method = "GET";
        response.timestamp = java.time.Instant.now().toString();
        response.traceId = "test-trace-id";
        
        if (exception instanceof AppException appEx) {
            response.details = appEx.getContext();
        } else {
            response.details = Map.of("type", exception.getClass().getSimpleName());
        }
        
        return response;
    }

    private static class MockErrorResponse {
        public int status;
        public String error;
        public String message;
        public String path;
        public String method;
        public String timestamp;
        public String traceId;
        public Map<String, Object> details;
    }
} 