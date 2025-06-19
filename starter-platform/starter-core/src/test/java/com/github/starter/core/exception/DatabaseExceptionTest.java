package com.github.starter.core.exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DatabaseException Unit Tests")
class DatabaseExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessageOnly() {
            String message = "Database connection failed";
            
            DatabaseException exception = new DatabaseException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            SQLException cause = new SQLException("Connection timeout", "08001", 1234);
            
            DatabaseException exception = new DatabaseException(cause);
            
            assertEquals(cause.toString(), exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Failed to execute query";
            SQLException cause = new SQLException("Table not found", "42S02", 1146);
            
            DatabaseException exception = new DatabaseException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "Database error occurred"})
        @DisplayName("Should handle various message inputs")
        void shouldHandleVariousMessageInputs(String message) {
            DatabaseException exception = new DatabaseException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle null cause gracefully")
        void shouldHandleNullCauseGracefully() {
            DatabaseException exception = new DatabaseException((Throwable) null);
            
            assertEquals(null, exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("SQL Exception Integration Tests")
    class SqlExceptionIntegrationTests {

        @Test
        @DisplayName("Should preserve SQL exception details")
        void shouldPreserveSqlExceptionDetails() {
            SQLException sqlEx = new SQLException("Duplicate entry", "23000", 1062);
            DatabaseException dbEx = new DatabaseException("Insert failed", sqlEx);
            
            assertEquals("Insert failed", dbEx.getMessage());
            assertEquals(sqlEx, dbEx.getCause());
            
            SQLException retrievedSqlEx = (SQLException) dbEx.getCause();
            assertEquals("Duplicate entry", retrievedSqlEx.getMessage());
            assertEquals("23000", retrievedSqlEx.getSQLState());
            assertEquals(1062, retrievedSqlEx.getErrorCode());
        }

        @Test
        @DisplayName("Should handle chained SQL exceptions")
        void shouldHandleChainedSqlExceptions() {
            SQLException rootCause = new SQLException("Connection lost", "08S01", 2013);
            SQLException nextEx = new SQLException("Transaction rolled back", "40001", 1213);
            nextEx.setNextException(rootCause);
            
            DatabaseException dbEx = new DatabaseException("Operation failed", nextEx);
            
            assertEquals("Operation failed", dbEx.getMessage());
            SQLException sqlEx = (SQLException) dbEx.getCause();
            assertEquals("Transaction rolled back", sqlEx.getMessage());
            assertNotNull(sqlEx.getNextException());
            assertEquals("Connection lost", sqlEx.getNextException().getMessage());
        }

        @Test
        @DisplayName("Should handle various SQL state categories")
        void shouldHandleVariousSqlStateCategories() {
            SQLException[] sqlExceptions = {
                new SQLException("Syntax error", "42000", 1064),
                new SQLException("Access denied", "28000", 1045),
                new SQLException("Connection failed", "08001", 2003),
                new SQLException("Constraint violation", "23000", 1062),
                new SQLException("Deadlock", "40001", 1213)
            };
            
            for (SQLException sqlEx : sqlExceptions) {
                DatabaseException dbEx = new DatabaseException("Wrapped SQL error", sqlEx);
                
                assertNotNull(dbEx.getCause());
                assertInstanceOf(SQLException.class, dbEx.getCause());
                assertEquals(sqlEx.getSQLState(), ((SQLException) dbEx.getCause()).getSQLState());
                assertEquals(sqlEx.getErrorCode(), ((SQLException) dbEx.getCause()).getErrorCode());
            }
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable and deserializable")
        void shouldBeSerializableAndDeserializable() throws IOException, ClassNotFoundException {
            DatabaseException original = new DatabaseException("Database error");
            
            byte[] serialized = serialize(original);
            DatabaseException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getCause(), deserialized.getCause());
        }

        @Test
        @DisplayName("Should maintain cause chain during serialization")
        void shouldMaintainCauseChainDuringSerialization() throws IOException, ClassNotFoundException {
            SQLException sqlCause = new SQLException("Connection timeout", "08001", 1234);
            DatabaseException original = new DatabaseException("Operation failed", sqlCause);
            
            byte[] serialized = serialize(original);
            DatabaseException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertNotNull(deserialized.getCause());
            assertInstanceOf(SQLException.class, deserialized.getCause());
            
            SQLException deserializedSqlEx = (SQLException) deserialized.getCause();
            assertEquals(sqlCause.getMessage(), deserializedSqlEx.getMessage());
            assertEquals(sqlCause.getSQLState(), deserializedSqlEx.getSQLState());
            assertEquals(sqlCause.getErrorCode(), deserializedSqlEx.getErrorCode());
        }

        @Test
        @DisplayName("Should handle serialization with complex cause chain")
        void shouldHandleSerializationWithComplexCauseChain() throws IOException, ClassNotFoundException {
            RuntimeException rootCause = new RuntimeException("Root cause");
            SQLException sqlCause = new SQLException("SQL error", "42000", 1064, rootCause);
            DatabaseException original = new DatabaseException("Database operation failed", sqlCause);
            
            byte[] serialized = serialize(original);
            DatabaseException deserialized = deserialize(serialized);
            
            assertEquals(original.getMessage(), deserialized.getMessage());
            
            SQLException deserializedSqlEx = (SQLException) deserialized.getCause();
            assertNotNull(deserializedSqlEx.getCause());
            assertEquals(rootCause.getMessage(), deserializedSqlEx.getCause().getMessage());
        }

        private byte[] serialize(DatabaseException exception) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(exception);
            }
            return baos.toByteArray();
        }

        private DatabaseException deserialize(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (DatabaseException) ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Exception Chain Tests")
    class ExceptionChainTests {

        @Test
        @DisplayName("Should maintain proper exception hierarchy")
        void shouldMaintainProperExceptionHierarchy() {
            DatabaseException dbEx = new DatabaseException("Test error");
            
            assertInstanceOf(RuntimeException.class, dbEx);
            assertInstanceOf(Exception.class, dbEx);
            assertInstanceOf(Throwable.class, dbEx);
        }

        @Test
        @DisplayName("Should handle deeply nested exception chains")
        void shouldHandleDeeplyNestedException() {
            Throwable current = new RuntimeException("Root");
            
            for (int i = 1; i <= 5; i++) {
                current = new SQLException("Level " + i, "42000", 1000 + i, current);
            }
            
            DatabaseException exception = new DatabaseException("Top level", current);
            
            int depth = 0;
            Throwable cause = exception.getCause();
            while (cause != null) {
                depth++;
                cause = cause.getCause();
            }
            
            assertEquals(6, depth);
        }

        @Test
        @DisplayName("Should preserve stack trace information")
        void shouldPreserveStackTraceInformation() {
            try {
                throw new SQLException("Test SQL error", "42000", 1064);
            } catch (SQLException sqlEx) {
                DatabaseException dbEx = new DatabaseException("Wrapper error", sqlEx);
                
                StackTraceElement[] stackTrace = dbEx.getStackTrace();
                assertNotNull(stackTrace);
                assertTrue(stackTrace.length > 0);
                
                StackTraceElement[] causeStackTrace = dbEx.getCause().getStackTrace();
                assertNotNull(causeStackTrace);
                assertTrue(causeStackTrace.length > 0);
            }
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
                DatabaseException exception = new DatabaseException("Test message " + i);
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
        @DisplayName("Should handle large exception chains efficiently")
        void shouldHandleLargeExceptionChainsEfficiently() {
            Throwable current = new RuntimeException("Root");
            
            long startTime = System.nanoTime();
            
            for (int i = 1; i <= 100; i++) {
                current = new SQLException("Level " + i, "42000", 1000 + i, current);
            }
            
            DatabaseException exception = new DatabaseException("Top level", current);
            
            long duration = System.nanoTime() - startTime;
            long durationMs = duration / 1_000_000;
            
            assertNotNull(exception.getCause());
            
            boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
            long threshold = isCI ? 500 : 50;
            
            assertTrue(durationMs < threshold, 
                "Creating exception with 100-level chain should take under " + threshold + "ms, took: " + durationMs + "ms");
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
                            SQLException sqlEx = new SQLException(
                                "SQL error " + j, 
                                "42000", 
                                1000 + j
                            );
                            DatabaseException dbEx = new DatabaseException(
                                "Thread " + threadId + " error " + j, 
                                sqlEx
                            );
                            
                            assertNotNull(dbEx.getMessage());
                            assertNotNull(dbEx.getCause());
                            assertInstanceOf(SQLException.class, dbEx.getCause());
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
    @DisplayName("Integration with Factory Methods Tests")
    class FactoryMethodsIntegrationTests {

        @Test
        @DisplayName("Should work with Exceptions factory methods")
        void shouldWorkWithExceptionsFactoryMethods() {
            SQLException sqlCause = new SQLException("Connection failed", "08001", 2003);
            
            DatabaseException dbEx1 = Exceptions.dbException(sqlCause);
            DatabaseException dbEx2 = Exceptions.dbException("Custom message", sqlCause);
            
            assertEquals(sqlCause.toString(), dbEx1.getMessage());
            assertEquals(sqlCause, dbEx1.getCause());
            
            assertEquals("Custom message", dbEx2.getMessage());
            assertEquals(sqlCause, dbEx2.getCause());
        }

        @Test
        @DisplayName("Should handle null arguments in factory methods")
        void shouldHandleNullArgumentsInFactoryMethods() {
            DatabaseException dbEx1 = Exceptions.dbException((Throwable) null);
            DatabaseException dbEx2 = Exceptions.dbException("Message", null);
            
            assertEquals(null, dbEx1.getMessage());
            assertNull(dbEx1.getCause());
            
            assertEquals("Message", dbEx2.getMessage());
            assertNull(dbEx2.getCause());
        }
    }
} 