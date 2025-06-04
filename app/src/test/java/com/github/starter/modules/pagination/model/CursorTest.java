package com.github.starter.modules.pagination.model;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@DisplayName("Cursor Tests")
class CursorTest {

    @Test
    @DisplayName("Should create valid cursor with string value")
    void shouldCreateValidCursorWithStringValue() {
        Cursor cursor = Cursor.create("test", 1L, SortDirection.ASC);
        assertNotNull(cursor);
        assertEquals("test", cursor.lastValue());
        assertEquals(1L, cursor.lastId());
        assertEquals(SortDirection.ASC, cursor.direction());
    }

    @Test
    @DisplayName("Should create valid cursor with numeric value")
    void shouldCreateValidCursorWithNumericValue() {
        Cursor cursor = Cursor.create(100, 1L, SortDirection.DESC);
        assertNotNull(cursor);
        assertEquals(100, cursor.lastValue());
        assertEquals(1L, cursor.lastId());
        assertEquals(SortDirection.DESC, cursor.direction());
    }

    @Test
    @DisplayName("Should throw exception when creating cursor with null direction")
    void shouldThrowExceptionWhenCreatingCursorWithNullDirection() {
        assertThrows(IllegalArgumentException.class,
            () -> Cursor.create("test", 1L, null),
            "Direction cannot be null"
        );
    }

    @Test
    @DisplayName("Should throw exception when creating cursor with invalid ID")
    void shouldThrowExceptionWhenCreatingCursorWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> Cursor.create("test", -1L, SortDirection.ASC),
            "Last ID must be positive"
        );
        
        assertThrows(IllegalArgumentException.class,
            () -> Cursor.create("test", 0L, SortDirection.ASC),
            "Last ID must be positive"
        );
        
        assertThrows(IllegalArgumentException.class,
            () -> Cursor.create("test", null, SortDirection.ASC),
            "Last ID must be positive"
        );
    }

    @Test
    @DisplayName("Should encode and decode cursor correctly")
    void shouldEncodeAndDecodeCursorCorrectly() {
        Cursor original = Cursor.create("test", 1L, SortDirection.ASC);
        String encoded = original.encode();
        Optional<Cursor> decoded = Cursor.decode(encoded);
        
        assertTrue(decoded.isPresent());
        Cursor decodedCursor = decoded.get();
        assertEquals(original.lastValue(), decodedCursor.lastValue());
        assertEquals(original.lastId(), decodedCursor.lastId());
        assertEquals(original.direction(), decodedCursor.direction());
    }

    @ParameterizedTest
    @MethodSource("provideValuesForEncoding")
    @DisplayName("Should encode and decode different value types correctly")
    void shouldEncodeAndDecodeDifferentValueTypesCorrectly(Object value, Long id, SortDirection direction) {
        Cursor original = Cursor.create(value, id, direction);
        String encoded = original.encode();
        Optional<Cursor> decoded = Cursor.decode(encoded);
        
        assertTrue(decoded.isPresent());
        Cursor decodedCursor = decoded.get();
        assertEquals(original.lastValue(), decodedCursor.lastValue());
        assertEquals(original.lastId(), decodedCursor.lastId());
        assertEquals(original.direction(), decodedCursor.direction());
    }

    private static Stream<Arguments> provideValuesForEncoding() {
        return Stream.of(
            Arguments.of("string value", 1L, SortDirection.ASC),
            Arguments.of(123, 2L, SortDirection.DESC),
            Arguments.of(123.45, 3L, SortDirection.ASC),
            Arguments.of(true, 4L, SortDirection.DESC),
            Arguments.of(null, 5L, SortDirection.ASC)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should return empty optional when decoding invalid cursor")
    void shouldReturnEmptyOptionalWhenDecodingInvalidCursor(String invalidCursor) {
        Optional<Cursor> decoded = Cursor.decode(invalidCursor);
        assertTrue(decoded.isEmpty());
    }

    @Test
    @DisplayName("Should return empty optional when decoding malformed cursor")
    void shouldReturnEmptyOptionalWhenDecodingMalformedCursor() {
        Optional<Cursor> decoded = Cursor.decode("not-a-valid-base64-cursor");
        assertTrue(decoded.isEmpty());
    }

    @Test
    @DisplayName("Should change direction correctly")
    void shouldChangeDirectionCorrectly() {
        Cursor original = Cursor.create("test", 1L, SortDirection.ASC);
        Cursor reversed = original.withDirection(SortDirection.DESC);
        
        assertEquals(original.lastValue(), reversed.lastValue());
        assertEquals(original.lastId(), reversed.lastId());
        assertEquals(SortDirection.DESC, reversed.direction());
    }

    @Test
    @DisplayName("Should throw exception when changing to null direction")
    void shouldThrowExceptionWhenChangingToNullDirection() {
        Cursor cursor = Cursor.create("test", 1L, SortDirection.ASC);
        assertThrows(IllegalArgumentException.class,
            () -> cursor.withDirection(null),
            "Direction cannot be null"
        );
    }
} 