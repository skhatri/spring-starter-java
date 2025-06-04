package com.github.starter.modules.pagination.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;
import com.github.starter.modules.pagination.repository.PaginatedRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultPaginationService Tests")
class DefaultPaginationServiceTest {

    @Mock
    private PaginatedRepository<TestEntity> repository;

    private DefaultPaginationService<TestEntity> paginationService;

    @BeforeEach
    void setUp() {
        paginationService = new DefaultPaginationService<>(repository);
    }

    @Test
    @DisplayName("Should find first page without cursor")
    void shouldFindFirstPageWithoutCursor() {
        PaginationRequest request = createPaginationRequest(null, 10, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(8);

        when(repository.findFirst(11, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(50L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(8, page.content().size());
                assertEquals(50L, page.totalCount());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
                assertTrue(page.nextCursor().isEmpty());
                assertTrue(page.previousCursor().isEmpty());
            })
            .verifyComplete();

        verify(repository).findFirst(11, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should find first page with more results available")
    void shouldFindFirstPageWithMoreResults() {
        PaginationRequest request = createPaginationRequest(null, 5, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(6);

        when(repository.findFirst(6, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(100L));
        when(repository.getFieldValue(entities.get(4), "name"))
            .thenReturn(Mono.just("Entity4"));
        when(repository.getId(entities.get(4)))
            .thenReturn(Mono.just(5L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(5, page.content().size());
                assertEquals(100L, page.totalCount());
                assertTrue(page.hasNext());
                assertFalse(page.hasPrevious());
                assertTrue(page.nextCursor().isPresent());
                assertTrue(page.previousCursor().isEmpty());
            })
            .verifyComplete();

        verify(repository).findFirst(6, "name", SortDirection.ASC);
        verify(repository).count();
        verify(repository).getFieldValue(entities.get(4), "name");
        verify(repository).getId(entities.get(4));
    }

    @Test
    @DisplayName("Should find page with cursor")
    void shouldFindPageWithCursor() {
        Cursor cursor = createTestCursor("Entity5", 5L, SortDirection.ASC);
        PaginationRequest request = createPaginationRequest(cursor, 5, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(5);

        when(repository.findWithCursor(cursor, 6, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(100L));
        when(repository.getFieldValue(entities.get(0), "name"))
            .thenReturn(Mono.just("Entity6"));
        when(repository.getId(entities.get(0)))
            .thenReturn(Mono.just(6L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(5, page.content().size());
                assertEquals(100L, page.totalCount());
                assertFalse(page.hasNext());
                assertTrue(page.hasPrevious());
                assertTrue(page.nextCursor().isEmpty());
                assertTrue(page.previousCursor().isPresent());
            })
            .verifyComplete();

        verify(repository).findWithCursor(cursor, 6, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should find page with cursor and more results")
    void shouldFindPageWithCursorAndMoreResults() {
        Cursor cursor = createTestCursor("Entity5", 5L, SortDirection.ASC);
        PaginationRequest request = createPaginationRequest(cursor, 3, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(4);

        when(repository.findWithCursor(cursor, 4, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(100L));
        when(repository.getFieldValue(entities.get(2), "name"))
            .thenReturn(Mono.just("Entity8"));
        when(repository.getId(entities.get(2)))
            .thenReturn(Mono.just(8L));
        when(repository.getFieldValue(entities.get(0), "name"))
            .thenReturn(Mono.just("Entity6"));
        when(repository.getId(entities.get(0)))
            .thenReturn(Mono.just(6L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(3, page.content().size());
                assertEquals(100L, page.totalCount());
                assertTrue(page.hasNext());
                assertTrue(page.hasPrevious());
                assertTrue(page.nextCursor().isPresent());
                assertTrue(page.previousCursor().isPresent());
            })
            .verifyComplete();

        verify(repository).findWithCursor(cursor, 4, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle empty results")
    void shouldHandleEmptyResults() {
        PaginationRequest request = createPaginationRequest(null, 10, "name", SortDirection.ASC);

        when(repository.findFirst(11, "name", SortDirection.ASC))
            .thenReturn(Flux.empty());
        when(repository.count())
            .thenReturn(Mono.just(0L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertTrue(page.isEmpty());
                assertEquals(0, page.content().size());
                assertEquals(0L, page.totalCount());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findFirst(11, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should get total count")
    void shouldGetTotalCount() {
        when(repository.count())
            .thenReturn(Mono.just(150L));

        Mono<Long> result = paginationService.getTotalCount();

        StepVerifier.create(result)
            .assertNext(count -> {
                assertEquals(150L, count);
            })
            .verifyComplete();

        verify(repository).count();
    }

    @Test
    @DisplayName("Should find first page with limit and sort field")
    void shouldFindFirstPageWithLimitAndSortField() {
        List<TestEntity> entities = createTestEntities(5);

        when(repository.findFirst(6, "email", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(25L));

        Mono<Page<TestEntity>> result = paginationService.findFirstPage(5, "email");

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(5, page.content().size());
                assertEquals(25L, page.totalCount());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findFirst(6, "email", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should find next page with cursor")
    void shouldFindNextPageWithCursor() {
        Cursor testCursor = createTestCursor("Entity5", 5L, SortDirection.ASC);
        String cursorString = testCursor.encode();
        List<TestEntity> entities = createTestEntities(3);

        when(repository.findWithCursor(any(Cursor.class), eq(4), eq("id"), eq(SortDirection.ASC)))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(50L));
        when(repository.getFieldValue(entities.get(0), "id"))
            .thenReturn(Mono.just(6L));
        when(repository.getId(entities.get(0)))
            .thenReturn(Mono.just(6L));

        Mono<Page<TestEntity>> result = paginationService.findNextPage(cursorString, 3);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(3, page.content().size());
                assertTrue(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findWithCursor(any(Cursor.class), eq(4), eq("id"), eq(SortDirection.ASC));
        verify(repository).count();
    }

    @Test
    @DisplayName("Should find previous page with cursor")
    void shouldFindPreviousPageWithCursor() {
        Cursor testCursor = createTestCursor("Entity5", 5L, SortDirection.ASC);
        String cursorString = testCursor.encode();
        List<TestEntity> entities = createTestEntities(3);

        when(repository.findWithCursor(any(Cursor.class), eq(4), eq("id"), eq(SortDirection.DESC)))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(50L));
        when(repository.getFieldValue(entities.get(0), "id"))
            .thenReturn(Mono.just(2L));
        when(repository.getId(entities.get(0)))
            .thenReturn(Mono.just(2L));

        Mono<Page<TestEntity>> result = paginationService.findPreviousPage(cursorString, 3);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(3, page.content().size());
                assertTrue(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findWithCursor(any(Cursor.class), eq(4), eq("id"), eq(SortDirection.DESC));
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle descending sort direction")
    void shouldHandleDescendingSortDirection() {
        PaginationRequest request = createPaginationRequest(null, 5, "createdAt", SortDirection.DESC);
        List<TestEntity> entities = createTestEntities(3);

        when(repository.findFirst(6, "createdAt", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(10L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(3, page.content().size());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findFirst(6, "createdAt", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle repository error")
    void shouldHandleRepositoryError() {
        PaginationRequest request = createPaginationRequest(null, 10, "name", SortDirection.ASC);

        when(repository.findFirst(11, "name", SortDirection.ASC))
            .thenReturn(Flux.error(new RuntimeException("Database connection failed")));
        when(repository.count())
            .thenReturn(Mono.just(50L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(repository).findFirst(11, "name", SortDirection.ASC);
    }

    @Test
    @DisplayName("Should handle count error")
    void shouldHandleCountError() {
        PaginationRequest request = createPaginationRequest(null, 5, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(3);

        when(repository.findFirst(6, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.error(new RuntimeException("Count query failed")));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(repository).findFirst(6, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle cursor generation error")
    void shouldHandleCursorGenerationError() {
        PaginationRequest request = createPaginationRequest(null, 3, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(4);

        when(repository.findFirst(4, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(50L));
        when(repository.getFieldValue(entities.get(2), "name"))
            .thenReturn(Mono.error(new RuntimeException("Field access failed")));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(repository).findFirst(4, "name", SortDirection.ASC);
        verify(repository).count();
        verify(repository).getFieldValue(entities.get(2), "name");
    }

    @Test
    @DisplayName("Should handle invalid cursor gracefully")
    void shouldHandleInvalidCursorGracefully() {
        PaginationRequest request = createPaginationRequest(null, 5, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(3);

        when(repository.findFirst(6, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(10L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(3, page.content().size());
            })
            .verifyComplete();

        verify(repository).findFirst(6, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle single item page")
    void shouldHandleSingleItemPage() {
        PaginationRequest request = createPaginationRequest(null, 10, "name", SortDirection.ASC);
        List<TestEntity> entities = Collections.singletonList(createTestEntity(1L, "SingleEntity"));

        when(repository.findFirst(11, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(1L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(1, page.content().size());
                assertEquals(1L, page.totalCount());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
                assertTrue(page.isSinglePage());
            })
            .verifyComplete();

        verify(repository).findFirst(11, "name", SortDirection.ASC);
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle exact limit match")
    void shouldHandleExactLimitMatch() {
        PaginationRequest request = createPaginationRequest(null, 5, "name", SortDirection.ASC);
        List<TestEntity> entities = createTestEntities(5);

        when(repository.findFirst(6, "name", SortDirection.ASC))
            .thenReturn(Flux.fromIterable(entities));
        when(repository.count())
            .thenReturn(Mono.just(5L));

        Mono<Page<TestEntity>> result = paginationService.findPage(request);

        StepVerifier.create(result)
            .assertNext(page -> {
                assertNotNull(page);
                assertEquals(5, page.content().size());
                assertEquals(5L, page.totalCount());
                assertFalse(page.hasNext());
                assertFalse(page.hasPrevious());
            })
            .verifyComplete();

        verify(repository).findFirst(6, "name", SortDirection.ASC);
        verify(repository).count();
    }

    private PaginationRequest createPaginationRequest(Cursor cursor, int limit, String sortField, SortDirection direction) {
        return new PaginationRequest(
            Optional.ofNullable(cursor),
            limit,
            direction,
            sortField
        );
    }

    private Cursor createTestCursor(String fieldValue, Long id, SortDirection direction) {
        return Cursor.create(fieldValue, id, direction);
    }

    private List<TestEntity> createTestEntities(int count) {
        return Arrays.asList(
            createTestEntity(1L, "Entity1"),
            createTestEntity(2L, "Entity2"),
            createTestEntity(3L, "Entity3"),
            createTestEntity(4L, "Entity4"),
            createTestEntity(5L, "Entity5"),
            createTestEntity(6L, "Entity6"),
            createTestEntity(7L, "Entity7"),
            createTestEntity(8L, "Entity8"),
            createTestEntity(9L, "Entity9"),
            createTestEntity(10L, "Entity10")
        ).subList(0, Math.min(count, 10));
    }

    private TestEntity createTestEntity(Long id, String name) {
        return new TestEntity(id, name, "test" + id + "@example.com");
    }

    private static class TestEntity {
        private final Long id;
        private final String name;
        private final String email;

        TestEntity(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
} 