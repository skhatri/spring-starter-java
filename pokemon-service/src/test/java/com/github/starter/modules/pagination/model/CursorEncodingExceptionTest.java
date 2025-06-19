package com.github.starter.modules.pagination.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CursorEncodingException Unit Tests")
class CursorEncodingExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("JSON parsing failed");
            String message = "Failed to encode cursor";
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            RuntimeException cause = new RuntimeException("Root cause");
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(null, cause);
            
            assertNull(exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            String message = "Encoding failed";
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(message, null);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "Valid error message"})
        @DisplayName("Should handle various message inputs")
        void shouldHandleVariousMessageInputs(String message) {
            RuntimeException cause = new RuntimeException("Test cause");
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("Should extend RuntimeException")
        void shouldExtendRuntimeException() {
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Test", new RuntimeException());
            
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(Exception.class, exception);
            assertInstanceOf(Throwable.class, exception);
        }

        @Test
        @DisplayName("Should be unchecked exception")
        void shouldBeUncheckedException() {
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Test", new RuntimeException());
            
            assertInstanceOf(RuntimeException.class, exception);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() throws IOException, ClassNotFoundException {
            RuntimeException cause = new RuntimeException("Original cause");
            Cursor.CursorEncodingException original = 
                new Cursor.CursorEncodingException("Encoding failed", cause);
            
            byte[] serialized = serialize(original);
            Cursor.CursorEncodingException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
        }

        @Test
        @DisplayName("Should maintain cause chain during serialization")
        void shouldMaintainCauseChainDuringSerialization() throws IOException, ClassNotFoundException {
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalArgumentException intermediateCause = new IllegalArgumentException("Intermediate", rootCause);
            Cursor.CursorEncodingException original = 
                new Cursor.CursorEncodingException("Top level", intermediateCause);
            
            byte[] serialized = serialize(original);
            Cursor.CursorEncodingException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(intermediateCause.getMessage(), deserialized.getCause().getMessage());
            assertEquals(rootCause.getMessage(), deserialized.getCause().getCause().getMessage());
        }

        private byte[] serialize(Cursor.CursorEncodingException exception) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(exception);
            }
            return baos.toByteArray();
        }

        private Cursor.CursorEncodingException deserialize(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (Cursor.CursorEncodingException) ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Stack Trace Tests")
    class StackTraceTests {

        @Test
        @DisplayName("Should preserve stack trace information")
        void shouldPreserveStackTraceInformation() {
            try {
                throw new RuntimeException("Simulated JSON error");
            } catch (RuntimeException cause) {
                Cursor.CursorEncodingException exception = 
                    new Cursor.CursorEncodingException("Cursor encoding failed", cause);
                
                StackTraceElement[] stackTrace = exception.getStackTrace();
                assertNotNull(stackTrace);
                assertTrue(stackTrace.length > 0);
                
                StackTraceElement[] causeStackTrace = exception.getCause().getStackTrace();
                assertNotNull(causeStackTrace);
                assertTrue(causeStackTrace.length > 0);
                
                assertEquals("shouldPreserveStackTraceInformation", stackTrace[0].getMethodName());
            }
        }

        @Test
        @DisplayName("Should support stack trace manipulation")
        void shouldSupportStackTraceManipulation() {
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Test", new RuntimeException());
            
            StackTraceElement[] newTrace = new StackTraceElement[1];
            newTrace[0] = new StackTraceElement("TestClass", "testMethod", "TestFile.java", 42);
            
            exception.setStackTrace(newTrace);
            
            StackTraceElement[] updatedTrace = exception.getStackTrace();
            assertEquals(1, updatedTrace.length);
            assertEquals("TestClass", updatedTrace[0].getClassName());
            assertEquals("testMethod", updatedTrace[0].getMethodName());
            assertEquals(42, updatedTrace[0].getLineNumber());
        }
    }

    @Nested
    @DisplayName("Message Handling Tests")
    class MessageHandlingTests {

        @Test
        @DisplayName("Should handle complex error messages")
        void shouldHandleComplexErrorMessages() {
            String complexMessage = "Failed to encode cursor: JSON processing error at line 42, column 15. " +
                "Invalid character '\\u0000' in field 'lastValue'. Expected valid UTF-8 sequence.";
            RuntimeException cause = new RuntimeException("Underlying JSON error");
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(complexMessage, cause);
            
            assertEquals(complexMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("JSON processing error"));
            assertTrue(exception.getMessage().contains("line 42"));
        }

        @Test
        @DisplayName("Should handle Unicode characters in message")
        void shouldHandleUnicodeCharactersInMessage() {
            String unicodeMessage = "编码失败: 游标处理错误 🚫";
            RuntimeException cause = new RuntimeException("Unicode test");
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(unicodeMessage, cause);
            
            assertEquals(unicodeMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("编码失败"));
            assertTrue(exception.getMessage().contains("🚫"));
        }

        @Test
        @DisplayName("Should handle very long error messages")
        void shouldHandleVeryLongErrorMessages() {
            StringBuilder longMessage = new StringBuilder("Cursor encoding failed: ");
            for (int i = 0; i < 1000; i++) {
                longMessage.append("Error detail ").append(i).append(". ");
            }
            String message = longMessage.toString();
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException(message, new RuntimeException());
            
            assertEquals(message, exception.getMessage());
            assertTrue(exception.getMessage().length() > 10000);
            assertTrue(exception.getMessage().startsWith("Cursor encoding failed"));
            assertTrue(exception.getMessage().contains("Error detail 999"));
        }
    }

    @Nested
    @DisplayName("Cause Chain Tests")
    class CauseChainTests {

        @Test
        @DisplayName("Should handle deeply nested cause chains")
        void shouldHandleDeeplyNestedCauseChains() {
            Throwable current = new RuntimeException("Root cause");
            
            for (int i = 1; i <= 10; i++) {
                current = new RuntimeException("Level " + i, current);
            }
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Cursor encoding failed", current);
            
            int depth = 0;
            Throwable cause = exception.getCause();
            while (cause != null) {
                depth++;
                cause = cause.getCause();
            }
            
            assertEquals(11, depth);
        }

        @Test
        @DisplayName("Should handle different exception types in cause chain")
        void shouldHandleDifferentExceptionTypesInCauseChain() {
            RuntimeException runtimeEx = new RuntimeException("Runtime level");
            IllegalArgumentException illegalArgEx = new IllegalArgumentException("Illegal arg level", runtimeEx);
            IllegalStateException illegalStateEx = new IllegalStateException("Illegal state level", illegalArgEx);
            
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Cursor encoding failed", illegalStateEx);
            
            assertInstanceOf(IllegalStateException.class, exception.getCause());
            assertInstanceOf(IllegalArgumentException.class, exception.getCause().getCause());
            assertInstanceOf(RuntimeException.class, exception.getCause().getCause().getCause());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should create exceptions efficiently")
        void shouldCreateExceptionsEfficiently() {
            RuntimeException cause = new RuntimeException("Test cause");
            
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 10000; i++) {
                Cursor.CursorEncodingException exception = 
                    new Cursor.CursorEncodingException("Message " + i, cause);
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
        @DisplayName("Should handle exception creation under concurrent load")
        void shouldHandleExceptionCreationUnderConcurrentLoad() throws InterruptedException {
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
                            RuntimeException cause = new RuntimeException("Thread " + threadId + " cause " + j);
                            Cursor.CursorEncodingException exception = 
                                new Cursor.CursorEncodingException("Thread " + threadId + " message " + j, cause);
                            
                            assertNotNull(exception.getMessage());
                            assertNotNull(exception.getCause());
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

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work in actual cursor encoding failure scenario")
        void shouldWorkInActualCursorEncodingFailureScenario() {
            assertThrows(Cursor.CursorEncodingException.class, () -> {
                throw new Cursor.CursorEncodingException("Cursor encoding failed", 
                    new RuntimeException("JSON parsing error"));
            });
        }

        @Test
        @DisplayName("Should provide useful error information")
        void shouldProvideUsefulErrorInformation() {
            RuntimeException jsonError = new RuntimeException("Invalid JSON: unexpected character at position 42");
            Cursor.CursorEncodingException exception = 
                new Cursor.CursorEncodingException("Failed to encode cursor data", jsonError);
            
            String errorInfo = exception.toString();
            
            assertTrue(errorInfo.contains("CursorEncodingException"));
            assertTrue(errorInfo.contains("Failed to encode cursor data"));
            
            String fullStackTrace = getStackTraceAsString(exception);
            assertTrue(fullStackTrace.contains("CursorEncodingException"));
            assertTrue(fullStackTrace.contains("RuntimeException"));
            assertTrue(fullStackTrace.contains("Invalid JSON"));
        }

        private String getStackTraceAsString(Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
} 