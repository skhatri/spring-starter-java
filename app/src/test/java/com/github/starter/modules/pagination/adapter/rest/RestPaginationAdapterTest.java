package com.github.starter.modules.pagination.adapter.rest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;

@DisplayName("RestPaginationAdapter Tests")
class RestPaginationAdapterTest {

    private RestPaginationAdapter adapter;
    private String validNextCursor;
    private String validPrevCursor;

    @BeforeEach
    void setUp() {
        adapter = new RestPaginationAdapter();
        validNextCursor = Cursor.create("value1", 1L, SortDirection.ASC).encode();
        validPrevCursor = Cursor.create("value2", 2L, SortDirection.ASC).encode();
    }

    @Test
    @DisplayName("Should create request with custom values")
    void shouldCreateRequestWithCustomValues() {
        PaginationRequest request = adapter.toPaginationRequest(25, validNextCursor, null, "name", "DESC");
        
        assertTrue(request.cursor().isPresent());
        assertEquals(25, request.limit());
        assertEquals(SortDirection.DESC, request.direction());
        assertEquals("name", request.sortField());
    }

    @Test
    @DisplayName("Should handle previous cursor")
    void shouldHandlePreviousCursor() {
        PaginationRequest request = adapter.toPaginationRequest(10, null, validPrevCursor, "id", "ASC");
        
        assertTrue(request.cursor().isPresent());
        assertEquals(10, request.limit());
        assertEquals(SortDirection.DESC, request.direction());
        assertEquals("id", request.sortField());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle null or empty cursors")
    void shouldHandleNullOrEmptyCursors(String cursor) {
        PaginationRequest request = adapter.toPaginationRequest(10, cursor, null, "id", "ASC");
        
        assertFalse(request.cursor().isPresent());
        assertEquals(10, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should create request with forward pagination")
    void shouldCreateRequestWithForwardPagination() {
        PaginationRequest request = adapter.toPaginationRequest(20, validNextCursor, null, "email", null);
        
        assertTrue(request.cursor().isPresent());
        assertEquals(20, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("email", request.sortField());
    }

    @Test
    @DisplayName("Should create request with backward pagination")
    void shouldCreateRequestWithBackwardPagination() {
        PaginationRequest request = adapter.toPaginationRequest(15, null, validPrevCursor, "createdAt", "ASC");
        
        assertTrue(request.cursor().isPresent());
        assertEquals(15, request.limit());
        assertEquals(SortDirection.DESC, request.direction());
        assertEquals("createdAt", request.sortField());
    }

    @Test
    @DisplayName("Should prioritize forward pagination over backward")
    void shouldPrioritizeForwardPaginationOverBackward() {
        PaginationRequest request = adapter.toPaginationRequest(30, validNextCursor, validPrevCursor, "status", "DESC");
        
        assertTrue(request.cursor().isPresent());
        assertEquals(30, request.limit());
        assertEquals(SortDirection.DESC, request.direction());
        assertEquals("status", request.sortField());
    }

    @ParameterizedTest
    @CsvSource({
        "ASC, ASC",
        "DESC, DESC",
        "asc, ASC",
        "desc, DESC",
        "ascending, ASC",
        "descending, DESC"
    })
    @DisplayName("Should parse sort direction correctly")
    void shouldParseSortDirectionCorrectly(String input, SortDirection expected) {
        PaginationRequest request = adapter.toPaginationRequest(10, null, null, "id", input);
        assertEquals(expected, request.direction());
    }

    @Test
    @DisplayName("Should handle null cursors with backward pagination")
    void shouldHandleNullCursorsWithBackwardPagination() {
        PaginationRequest request = adapter.toPaginationRequest(10, null, null, "id", "ASC");
        
        assertFalse(request.cursor().isPresent());
        assertEquals(10, request.limit());
        assertEquals(SortDirection.ASC, request.direction());
        assertEquals("id", request.sortField());
    }

    @Test
    @DisplayName("Should create paginated response")
    void shouldCreatePaginatedResponse() {
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = Page.create(
            content,
            validNextCursor,
            null,
            true,
            false,
            10
        );
        PaginationRequest request = PaginationRequest.defaultRequest();
        
        PaginatedResponse<String> response = adapter.toPaginatedResponse(page, request, "http://localhost/api");
        
        assertEquals(content, response.data());
        assertTrue(response.pagination().hasNext());
        assertFalse(response.pagination().hasPrevious());
    }
} 