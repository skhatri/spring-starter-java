package com.github.starter.modules.pagination.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PaginationRequest Tests")
class PaginationRequestTest {

    @Test
    @DisplayName("Should create default request with correct values")
    void shouldCreateDefaultRequestWithCorrectValues() {
        PaginationRequest request = PaginationRequest.defaultRequest();
        
        assertFalse(request.hasCursor());
        assertEquals(50, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should create request with custom limit")
    void shouldCreateRequestWithCustomLimit() {
        PaginationRequest request = PaginationRequest.withLimit(25);
        
        assertFalse(request.hasCursor());
        assertEquals(25, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should enforce maximum limit")
    void shouldEnforceMaximumLimit() {
        PaginationRequest request = PaginationRequest.withLimit(150);
        assertEquals(100, request.limit());
    }

    @Test
    @DisplayName("Should enforce minimum limit")
    void shouldEnforceMinimumLimit() {
        PaginationRequest request = PaginationRequest.withLimit(-10);
        assertEquals(50, request.limit());
    }

    @Test
    @DisplayName("Should create request with cursor")
    void shouldCreateRequestWithCursor() {
        Cursor cursor = Cursor.create("test", 1L, SortDirection.ASC);
        String encodedCursor = cursor.encode();
        
        PaginationRequest request = PaginationRequest.withCursor(encodedCursor);
        
        assertTrue(request.hasCursor());
        assertEquals(50, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
        assertTrue(request.cursor().isPresent());
        assertEquals(cursor.lastValue(), request.cursor().get().lastValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle invalid cursor strings")
    void shouldHandleInvalidCursorStrings(String invalidCursor) {
        PaginationRequest request = PaginationRequest.withCursor(invalidCursor);
        
        assertFalse(request.hasCursor());
        assertEquals(50, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should create request with all custom parameters")
    void shouldCreateRequestWithAllCustomParameters() {
        Cursor cursor = Cursor.create("test", 1L, SortDirection.ASC);
        String encodedCursor = cursor.encode();
        
        PaginationRequest request = PaginationRequest.create(
            encodedCursor,
            25,
            "name",
            SortDirection.DESC
        );
        
        assertTrue(request.hasCursor());
        assertEquals(25, request.limit());
        assertEquals(SortDirection.DESC, request.direction());
        assertEquals("name", request.sortField());
        assertTrue(request.cursor().isPresent());
        assertEquals(cursor.lastValue(), request.cursor().get().lastValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "unknown", "notAllowed"})
    @DisplayName("Should throw exception for invalid sort fields")
    void shouldThrowExceptionForInvalidSortFields(String invalidField) {
        assertThrows(IllegalArgumentException.class,
            () -> PaginationRequest.create(null, 50, invalidField, SortDirection.ASC),
            "Invalid sort field: " + invalidField
        );
    }

    @Test
    @DisplayName("Should handle null parameters with defaults")
    void shouldHandleNullParametersWithDefaults() {
        PaginationRequest request = PaginationRequest.create(
            null,
            null,
            null,
            null
        );
        
        assertFalse(request.hasCursor());
        assertEquals(50, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should correctly identify pagination direction")
    void shouldCorrectlyIdentifyPaginationDirection() {
        PaginationRequest forward = PaginationRequest.create(null, 50, "id", SortDirection.ASC);
        PaginationRequest backward = PaginationRequest.create(null, 50, "id", SortDirection.DESC);
        
        assertTrue(forward.isForwardPagination());
        assertFalse(forward.isBackwardPagination());
        
        assertTrue(backward.isBackwardPagination());
        assertFalse(backward.isForwardPagination());
    }

    @Test
    @DisplayName("Should reverse pagination direction")
    void shouldReversePaginationDirection() {
        PaginationRequest original = PaginationRequest.create(null, 50, "id", SortDirection.ASC);
        PaginationRequest reversed = original.reverse();
        
        assertEquals(SortDirection.DESC, reversed.direction());
        assertEquals(original.limit(), reversed.limit());
        assertEquals(original.sortField(), reversed.sortField());
        assertEquals(original.cursor(), reversed.cursor());
    }

    @Test
    @DisplayName("Should create new request with updated cursor")
    void shouldCreateNewRequestWithUpdatedCursor() {
        PaginationRequest original = PaginationRequest.defaultRequest();
        Cursor newCursor = Cursor.create("test", 1L, SortDirection.ASC);
        
        PaginationRequest updated = original.withCursor(newCursor);
        
        assertTrue(updated.hasCursor());
        assertEquals(newCursor, updated.cursor().get());
        assertEquals(original.limit(), updated.limit());
        assertEquals(original.direction(), updated.direction());
        assertEquals(original.sortField(), updated.sortField());
    }

    @Test
    @DisplayName("Should create new request with updated sort field")
    void shouldCreateNewRequestWithUpdatedSortField() {
        PaginationRequest original = PaginationRequest.defaultRequest();
        PaginationRequest updated = original.withSortField("name");
        
        assertEquals("name", updated.sortField());
        assertEquals(original.limit(), updated.limit());
        assertEquals(original.direction(), updated.direction());
        assertEquals(original.cursor(), updated.cursor());
    }

    @Test
    @DisplayName("Should create new request with updated direction")
    void shouldCreateNewRequestWithUpdatedDirection() {
        PaginationRequest original = PaginationRequest.defaultRequest();
        PaginationRequest updated = original.withDirection(SortDirection.DESC);
        
        assertEquals(SortDirection.DESC, updated.direction());
        assertEquals(original.limit(), updated.limit());
        assertEquals(original.sortField(), updated.sortField());
        assertEquals(original.cursor(), updated.cursor());
    }
} 