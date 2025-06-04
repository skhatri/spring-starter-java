package com.github.starter.modules.pagination.model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Page Tests")
class PageTest {

    @Test
    @DisplayName("Should create empty page")
    void shouldCreateEmptyPage() {
        Page<String> page = Page.empty();
        
        assertTrue(page.isEmpty());
        assertTrue(page.content().isEmpty());
        assertFalse(page.nextCursor().isPresent());
        assertFalse(page.previousCursor().isPresent());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
        assertEquals(0, page.totalCount());
        assertEquals(0, page.pageSize());
    }

    @Test
    @DisplayName("Should create single page")
    void shouldCreateSinglePage() {
        List<String> content = List.of("item1", "item2");
        Page<String> page = Page.single(content, 2);
        
        assertFalse(page.isEmpty());
        assertEquals(content, page.content());
        assertFalse(page.nextCursor().isPresent());
        assertFalse(page.previousCursor().isPresent());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
        assertEquals(2, page.totalCount());
        assertEquals(2, page.pageSize());
    }

    @Test
    @DisplayName("Should create page with all parameters")
    void shouldCreatePageWithAllParameters() {
        List<String> content = List.of("item1", "item2");
        Page<String> page = Page.create(
            content,
            "nextCursor",
            "prevCursor",
            true,
            true,
            10
        );
        
        assertFalse(page.isEmpty());
        assertEquals(content, page.content());
        assertTrue(page.nextCursor().isPresent());
        assertEquals("nextCursor", page.nextCursor().get());
        assertTrue(page.previousCursor().isPresent());
        assertEquals("prevCursor", page.previousCursor().get());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
        assertEquals(10, page.totalCount());
        assertEquals(2, page.pageSize());
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void shouldHandleNullValuesInConstructor() {
        Page<String> page = new Page<>(
            null,
            null,
            null,
            true,
            true,
            10,
            5
        );
        
        assertTrue(page.isEmpty());
        assertTrue(page.content().isEmpty());
        assertFalse(page.nextCursor().isPresent());
        assertFalse(page.previousCursor().isPresent());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
        assertEquals(10, page.totalCount());
        assertEquals(0, page.pageSize());
    }

    @Test
    @DisplayName("Should handle negative values in constructor")
    void shouldHandleNegativeValuesInConstructor() {
        Page<String> page = new Page<>(
            List.of("item"),
            Optional.empty(),
            Optional.empty(),
            false,
            false,
            -1,
            -1
        );
        
        assertFalse(page.isEmpty());
        assertEquals(1, page.content().size());
        assertEquals(0, page.totalCount());
        assertEquals(0, page.pageSize());
    }

    @Test
    @DisplayName("Should map content correctly")
    void shouldMapContentCorrectly() {
        Page<String> stringPage = Page.create(
            List.of("1", "2"),
            "next",
            "prev",
            true,
            true,
            10
        );
        
        Page<Integer> intPage = stringPage.map(Integer::parseInt);
        
        assertEquals(List.of(1, 2), intPage.content());
        assertEquals(stringPage.nextCursor(), intPage.nextCursor());
        assertEquals(stringPage.previousCursor(), intPage.previousCursor());
        assertEquals(stringPage.hasNext(), intPage.hasNext());
        assertEquals(stringPage.hasPrevious(), intPage.hasPrevious());
        assertEquals(stringPage.totalCount(), intPage.totalCount());
        assertEquals(stringPage.pageSize(), intPage.pageSize());
    }

    @Test
    @DisplayName("Should identify page position correctly")
    void shouldIdentifyPagePositionCorrectly() {
        Page<String> firstPage = Page.create(
            List.of("item1"),
            "next",
            null,
            true,
            false,
            10
        );
        
        Page<String> middlePage = Page.create(
            List.of("item2"),
            "next",
            "prev",
            true,
            true,
            10
        );
        
        Page<String> lastPage = Page.create(
            List.of("item3"),
            null,
            "prev",
            false,
            true,
            10
        );
        
        assertTrue(firstPage.isFirstPage());
        assertFalse(firstPage.isLastPage());
        assertFalse(firstPage.isSinglePage());
        
        assertFalse(middlePage.isFirstPage());
        assertFalse(middlePage.isLastPage());
        assertFalse(middlePage.isSinglePage());
        
        assertFalse(lastPage.isFirstPage());
        assertTrue(lastPage.isLastPage());
        assertFalse(lastPage.isSinglePage());
    }

    @Test
    @DisplayName("Should identify single page correctly")
    void shouldIdentifySinglePageCorrectly() {
        Page<String> singlePage = Page.create(
            List.of("item"),
            null,
            null,
            false,
            false,
            1
        );
        
        assertTrue(singlePage.isFirstPage());
        assertTrue(singlePage.isLastPage());
        assertTrue(singlePage.isSinglePage());
    }

    @Test
    @DisplayName("Should get first and last items correctly")
    void shouldGetFirstAndLastItemsCorrectly() {
        List<String> content = List.of("first", "middle", "last");
        Page<String> page = Page.single(content, 3);
        
        assertTrue(page.getFirst().isPresent());
        assertEquals("first", page.getFirst().get());
        
        assertTrue(page.getLast().isPresent());
        assertEquals("last", page.getLast().get());
    }

    @Test
    @DisplayName("Should handle empty content for first and last items")
    void shouldHandleEmptyContentForFirstAndLastItems() {
        Page<String> emptyPage = Page.empty();
        
        assertFalse(emptyPage.getFirst().isPresent());
        assertFalse(emptyPage.getLast().isPresent());
    }

    @Test
    @DisplayName("Should update total count correctly")
    void shouldUpdateTotalCountCorrectly() {
        Page<String> original = Page.single(List.of("item"), 1);
        Page<String> updated = original.withTotalCount(100);
        
        assertEquals(original.content(), updated.content());
        assertEquals(original.nextCursor(), updated.nextCursor());
        assertEquals(original.previousCursor(), updated.previousCursor());
        assertEquals(original.hasNext(), updated.hasNext());
        assertEquals(original.hasPrevious(), updated.hasPrevious());
        assertEquals(100, updated.totalCount());
        assertEquals(original.pageSize(), updated.pageSize());
    }
} 