package com.github.starter.core.exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AppException Unit Tests")
class AppExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessageOnly() {
            String message = "Test error message";
            
            AppException exception = new AppException(message);
            
            assertEquals(message, exception.getMessage());
            assertEquals("APP_ERROR", exception.getErrorCode());
            assertTrue(exception.getContext().isEmpty());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            RuntimeException cause = new RuntimeException("Root cause");
            
            AppException exception = new AppException(cause);
            
            assertEquals(cause.toString(), exception.getMessage());
            assertEquals("APP_ERROR", exception.getErrorCode());
            assertTrue(exception.getContext().isEmpty());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Custom error message";
            RuntimeException cause = new RuntimeException("Root cause");
            
            AppException exception = new AppException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals("APP_ERROR", exception.getErrorCode());
            assertTrue(exception.getContext().isEmpty());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with all parameters")
        void shouldCreateExceptionWithAllParameters() {
            String errorCode = "VALIDATION_ERROR";
            String message = "Validation failed";
            Map<String, Object> context = Map.of(
                "field", "email",
                "value", "invalid-email",
                "errorCount", 1
            );
            
            AppException exception = new AppException(errorCode, message, context);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(context, exception.getContext());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle null context gracefully")
        void shouldHandleNullContextGracefully() {
            AppException exception = new AppException("CUSTOM_ERROR", "Test message", null);
            
            assertNotNull(exception.getContext());
            assertTrue(exception.getContext().isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should handle various message inputs")
        void shouldHandleVariousMessageInputs(String message) {
            AppException exception = new AppException(message);
            
            assertEquals(message, exception.getMessage());
            assertEquals("APP_ERROR", exception.getErrorCode());
            assertTrue(exception.getContext().isEmpty());
        }
    }

    @Nested
    @DisplayName("Context Handling Tests")
    class ContextHandlingTests {

        @Test
        @DisplayName("Should create immutable copy of context")
        void shouldCreateImmutableCopyOfContext() {
            Map<String, Object> originalContext = new HashMap<>();
            originalContext.put("field", "email");
            originalContext.put("value", "test@example.com");
            
            AppException exception = new AppException("VALIDATION_ERROR", "Test", originalContext);
            
            originalContext.put("field", "modified");
            
            assertEquals("email", exception.getContext().get("field"));
            assertNotSame(originalContext, exception.getContext());
        }

        @Test
        @DisplayName("Should handle complex context objects")
        void shouldHandleComplexContextObjects() {
            Map<String, Object> nestedMap = Map.of("nested", "value");
            Map<String, Object> context = Map.of(
                "string", "text",
                "number", 42,
                "boolean", true,
                "nested", nestedMap
            );
            
            AppException exception = new AppException("COMPLEX_ERROR", "Test", context);
            
            Map<String, Object> resultContext = exception.getContext();
            assertEquals("text", resultContext.get("string"));
            assertEquals(42, resultContext.get("number"));
            assertEquals(true, resultContext.get("boolean"));
            assertEquals(nestedMap, resultContext.get("nested"));
            assertNull(resultContext.get("nonExistentKey"));
        }

        @Test
        @DisplayName("Should prevent context modification")
        void shouldPreventContextModification() {
            Map<String, Object> context = Map.of("field", "value");
            AppException exception = new AppException("ERROR", "Test", context);
            
            assertThrows(UnsupportedOperationException.class, () -> {
                exception.getContext().put("newField", "newValue");
            });
        }

        @Test
        @DisplayName("Should handle empty context correctly")
        void shouldHandleEmptyContextCorrectly() {
            Map<String, Object> emptyContext = Map.of();
            AppException exception = new AppException("ERROR", "Test", emptyContext);
            
            assertTrue(exception.getContext().isEmpty());
            assertEquals(0, exception.getContext().size());
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable and deserializable")
        void shouldBeSerializableAndDeserializable() throws IOException, ClassNotFoundException {
            Map<String, Object> context = Map.of(
                "field", "email",
                "errorCode", 400
            );
            AppException original = new AppException("VALIDATION_ERROR", "Test message", context);
            
            byte[] serialized = serialize(original);
            AppException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getContext(), deserialized.getContext());
        }

        @Test
        @DisplayName("Should maintain cause chain during serialization")
        void shouldMaintainCauseChainDuringSerialization() throws IOException, ClassNotFoundException {
            RuntimeException rootCause = new RuntimeException("Root cause");
            AppException original = new AppException("Wrapper message", rootCause);
            
            byte[] serialized = serialize(original);
            AppException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertNotNull(deserialized.getCause());
            assertEquals(rootCause.getMessage(), deserialized.getCause().getMessage());
        }

        @Test
        @DisplayName("Should handle serialization with serializable context")
        void shouldHandleSerializationWithSerializableContext() throws IOException, ClassNotFoundException {
            Map<String, Object> context = Map.of(
                "field", "email",
                "count", 42
            );
            AppException original = new AppException("ERROR", "Test", context);
            
            byte[] serialized = serialize(original);
            AppException deserialized = deserialize(serialized);
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
        }

        private byte[] serialize(AppException exception) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(exception);
            }
            return baos.toByteArray();
        }

        private AppException deserialize(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (AppException) ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Error Code Tests")
    class ErrorCodeTests {

        @ParameterizedTest
        @MethodSource("errorCodeProvider")
        @DisplayName("Should handle various error codes")
        void shouldHandleVariousErrorCodes(String errorCode, String expectedCode) {
            AppException exception = new AppException(errorCode, "Test message", Map.of());
            
            assertEquals(expectedCode, exception.getErrorCode());
        }

        static Stream<Arguments> errorCodeProvider() {
            return Stream.of(
                Arguments.of("VALIDATION_ERROR", "VALIDATION_ERROR"),
                Arguments.of("DATABASE_ERROR", "DATABASE_ERROR"),
                Arguments.of("BUSINESS_RULE_VIOLATION", "BUSINESS_RULE_VIOLATION"),
                Arguments.of("", ""),
                Arguments.of("SUPER_LONG_ERROR_CODE_THAT_IS_VERY_DESCRIPTIVE", "SUPER_LONG_ERROR_CODE_THAT_IS_VERY_DESCRIPTIVE"),
                Arguments.of("123", "123"),
                Arguments.of("UNICODE_错误", "UNICODE_错误")
            );
        }

        @Test
        @DisplayName("Should handle null error code")
        void shouldHandleNullErrorCode() {
            AppException exception = new AppException(null, "Test message", Map.of());
            
            assertNull(exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Exception Chain Tests")
    class ExceptionChainTests {

        @Test
        @DisplayName("Should maintain proper exception chain")
        void shouldMaintainProperExceptionChain() {
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalArgumentException intermediateCause = new IllegalArgumentException("Intermediate", rootCause);
            AppException topLevel = new AppException("Top level", intermediateCause);
            
            assertEquals(intermediateCause, topLevel.getCause());
            assertEquals(rootCause, topLevel.getCause().getCause());
            assertNull(topLevel.getCause().getCause().getCause());
        }

        @Test
        @DisplayName("Should handle deeply nested exception chains")
        void shouldHandleDeeplyNestedException() {
            Throwable current = new RuntimeException("Root");
            
            for (int i = 1; i <= 10; i++) {
                current = new RuntimeException("Level " + i, current);
            }
            
            AppException exception = new AppException("Final wrapper", current);
            
            int depth = 0;
            Throwable cause = exception.getCause();
            while (cause != null) {
                depth++;
                cause = cause.getCause();
            }
            
            assertEquals(11, depth);
        }

        @Test
        @DisplayName("Should handle self-cause prevention")
        void shouldHandleSelfCausePrevention() {
            AppException exception = new AppException("Exception");
            
            assertThrows(IllegalArgumentException.class, () -> {
                exception.initCause(exception);
            });
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should create exceptions efficiently")
        void shouldCreateExceptionsEfficiently() {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 10000; i++) {
                AppException exception = new AppException("Test message " + i);
                assertNotNull(exception.getMessage());
            }
            
            long duration = System.nanoTime() - startTime;
            long durationMs = duration / 1_000_000;
            
            boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
            long threshold = isCI ? 1000 : 100;
            
            assertTrue(durationMs < threshold, 
                "Creating 10,000 exceptions should take under " + threshold + "ms, took: " + durationMs + "ms");
        }

        @Test
        @DisplayName("Should handle large context efficiently")
        void shouldHandleLargeContextEfficiently() {
            Map<String, Object> largeContext = new HashMap<>();
            for (int i = 0; i < 1000; i++) {
                largeContext.put("key" + i, "value" + i);
            }
            
            long startTime = System.nanoTime();
            AppException exception = new AppException("ERROR", "Test", largeContext);
            long duration = System.nanoTime() - startTime;
            
            assertNotNull(exception.getContext());
            assertEquals(1000, exception.getContext().size());
            assertTrue(duration < 10_000_000, 
                "Creating exception with large context should take under 10ms");
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe during creation")
        void shouldBeThreadSafeDuringCreation() throws InterruptedException {
            int threadCount = 10;
            int exceptionsPerThread = 100;
            Thread[] threads = new Thread[threadCount];
            Exception[] threadExceptions = new Exception[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                final int finalExceptionsPerThread = exceptionsPerThread;
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < finalExceptionsPerThread; j++) {
                            Map<String, Object> context = Map.of(
                                "threadId", threadId,
                                "iteration", j
                            );
                            AppException exception = new AppException("THREAD_ERROR", 
                                "Thread " + threadId + " iteration " + j, context);
                            
                            assertEquals("THREAD_ERROR", exception.getErrorCode());
                            assertEquals(2, exception.getContext().size());
                            assertEquals(threadId, exception.getContext().get("threadId"));
                            assertEquals(j, exception.getContext().get("iteration"));
                        }
                    } catch (Exception e) {
                        threadExceptions[threadId] = e;
                    }
                });
            }
            
            for (int i = 0; i < threadCount; i++) {
                threads[i].start();
            }
            
            for (int i = 0; i < threadCount; i++) {
                threads[i].join();
            }
            
            for (int i = 0; i < threadCount; i++) {
                assertNull(threadExceptions[i], 
                    "Thread " + i + " should not have thrown exception: " + threadExceptions[i]);
            }
        }
    }
} 