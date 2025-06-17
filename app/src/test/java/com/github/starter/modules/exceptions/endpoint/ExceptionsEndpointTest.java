package com.github.starter.modules.exceptions.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.github.starter.core.exception.AppException;
import com.github.starter.core.exception.DatabaseException;
import com.github.starter.test.base.BaseUnitTest;

@DisplayName("Exceptions Endpoint Tests")
class ExceptionsEndpointTest extends BaseUnitTest {

    private ExceptionsEndpoint exceptionsEndpoint;

    @BeforeEach
    void setUp() {
        exceptionsEndpoint = new ExceptionsEndpoint();
    }

    @Test
    @DisplayName("Should throw AppException for 400 error endpoint")
    void shouldThrowAppExceptionFor400Error() {
        AppException exception = assertThrows(AppException.class, () -> {
            exceptionsEndpoint.trigger400Error("Test 400 error");
        });

        assertEquals("BAD_REQUEST", exception.getErrorCode());
        assertEquals("Test 400 error", exception.getMessage());
        assertTrue(exception.getContext().containsKey("errorCode"));
        assertEquals(400, exception.getContext().get("errorCode"));
    }

    @Test
    @DisplayName("Should throw AppException for 400 error with default message")
    void shouldThrowAppExceptionFor400ErrorWithDefaultMessage() {
        AppException exception = assertThrows(AppException.class, () -> {
            exceptionsEndpoint.trigger400Error("Bad Request Error");
        });

        assertEquals("BAD_REQUEST", exception.getErrorCode());
        assertEquals("Bad Request Error", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for 404 error endpoint")
    void shouldThrowIllegalArgumentExceptionFor404Error() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exceptionsEndpoint.trigger404Error("Test 404 error");
        });

        assertEquals("Test 404 error", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw DatabaseException for 500 error endpoint")
    void shouldThrowDatabaseExceptionFor500Error() {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            exceptionsEndpoint.trigger500Error("Test 500 error");
        });

        assertEquals("Test 500 error", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Database connection failed", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException for runtime error endpoint")
    void shouldThrowRuntimeExceptionForRuntimeError() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exceptionsEndpoint.triggerRuntimeError("Test runtime error");
        });

        assertEquals("Test runtime error", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException for null pointer endpoint")
    void shouldThrowNullPointerExceptionForNullPointer() {
        assertThrows(NullPointerException.class, () -> {
            exceptionsEndpoint.triggerNullPointerError();
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for custom 400 status")
    void shouldThrowIllegalArgumentExceptionForCustom400Status() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exceptionsEndpoint.triggerCustomStatusError(400, "Custom 400 message");
        });

        assertEquals("Custom 400 message (Custom 400)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException for custom 500 status")
    void shouldThrowRuntimeExceptionForCustom500Status() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exceptionsEndpoint.triggerCustomStatusError(500, "Custom 500 message");
        });

        assertEquals("Custom 500 message (Custom 500)", exception.getMessage());
    }

    @Test
    @DisplayName("Should return success response for custom 200 status")
    void shouldReturnSuccessResponseForCustom200Status() {
        ResponseEntity<String> result = exceptionsEndpoint.triggerCustomStatusError(200, "Success message");
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Custom status: 200", result.getBody());
    }

    @Test
    @DisplayName("Should return success response for success endpoint")
    void shouldReturnSuccessResponseForSuccessEndpoint() {
        ResponseEntity<Map<String, Object>> result = exceptionsEndpoint.success();
        
        assertEquals(200, result.getStatusCode().value());
        
        Map<String, Object> body = result.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("All systems operational", body.get("message"));
        assertEquals("/exceptions/success", body.get("endpoint"));
        assertNotNull(body.get("timestamp"));
        assertTrue(body.get("timestamp") instanceof Long);
    }
} 