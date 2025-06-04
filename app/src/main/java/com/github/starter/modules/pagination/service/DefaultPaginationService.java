package com.github.starter.modules.pagination.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;
import com.github.starter.modules.pagination.repository.PaginatedRepository;

import reactor.core.publisher.Mono;

public class DefaultPaginationService<T> implements PaginationService<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultPaginationService.class);
    
    private final PaginatedRepository<T> repository;
    private final CursorGenerator<T> cursorGenerator;
    
    public DefaultPaginationService(PaginatedRepository<T> repository) {
        this.repository = repository;
        this.cursorGenerator = new CursorGenerator<>(repository);
    }
    
    @Override
    public Mono<Page<T>> findPage(PaginationRequest request) {
        logger.debug("Finding page with request: {}", request);
        
        return request.hasCursor() 
            ? findPageWithCursor(request)
            : findFirstPage(request.limit(), request.sortField());
    }
    
    @Override
    public Mono<Long> getTotalCount() {
        return repository.count()
            .doOnNext(count -> logger.debug("Total count: {}", count));
    }
    
    @Override
    public Mono<Page<T>> findFirstPage(int limit, String sortField) {
        return findPageWithRequest(
            PaginationRequest.create(null, limit, sortField, SortDirection.ASC)
        );
    }
    
    @Override
    public Mono<Page<T>> findNextPage(String cursor, int limit) {
        return findPageWithRequest(
            PaginationRequest.create(cursor, limit, "id", SortDirection.ASC)
        );
    }
    
    @Override
    public Mono<Page<T>> findPreviousPage(String cursor, int limit) {
        return findPageWithRequest(
            PaginationRequest.create(cursor, limit, "id", SortDirection.DESC)
        );
    }
    
    private Mono<Page<T>> findPageWithCursor(PaginationRequest request) {
        return request.cursor()
            .map(cursor -> findPageWithValidCursor(request, cursor))
            .orElseGet(() -> findFirstPage(request.limit(), request.sortField()));
    }
    
    private Mono<Page<T>> findPageWithValidCursor(PaginationRequest request, Cursor cursor) {
        return repository.findWithCursor(
                cursor, 
                request.limit() + 1, 
                request.sortField(), 
                request.direction()
            )
            .collectList()
            .zipWith(getTotalCount())
            .flatMap(tuple -> buildPageAsync(tuple.getT1(), request, tuple.getT2()));
    }
    
    private Mono<Page<T>> findPageWithRequest(PaginationRequest request) {
        if (request.hasCursor()) {
            return findPageWithCursor(request);
        }
        
        return repository.findFirst(
                request.limit() + 1,
                request.sortField(),
                request.direction()
            )
            .collectList()
            .zipWith(getTotalCount())
            .flatMap(tuple -> buildPageAsync(tuple.getT1(), request, tuple.getT2()));
    }
    
    private Mono<Page<T>> buildPageAsync(List<T> results, PaginationRequest request, long totalCount) {
        if (results.isEmpty()) {
            return Mono.just(Page.empty());
        }
        
        boolean hasNext = results.size() > request.limit();
        List<T> content = hasNext 
            ? results.subList(0, request.limit())
            : results;
        
        Mono<Optional<String>> nextCursorMono = hasNext
            ? generateNextCursor(content, request)
            : Mono.just(Optional.empty());
            
        Mono<Optional<String>> previousCursorMono = request.hasCursor()
            ? generatePreviousCursor(content, request)
            : Mono.just(Optional.empty());
            
        boolean hasPrevious = request.hasCursor();
        
        return Mono.zip(nextCursorMono, previousCursorMono)
            .map(tuple -> Page.create(
                content,
                tuple.getT1().orElse(null),
                tuple.getT2().orElse(null),
                hasNext,
                hasPrevious,
                totalCount
            ));
    }
    
    private Mono<Optional<String>> generateNextCursor(List<T> content, PaginationRequest request) {
        if (content.isEmpty()) {
            return Mono.just(Optional.empty());
        }
        
        T lastItem = content.get(content.size() - 1);
        return cursorGenerator.generateCursor(lastItem, request.sortField(), request.direction())
            .map(cursor -> Optional.of(cursor.encode()));
    }
    
    private Mono<Optional<String>> generatePreviousCursor(List<T> content, PaginationRequest request) {
        if (content.isEmpty()) {
            return Mono.just(Optional.empty());
        }
        
        T firstItem = content.get(0);
        return cursorGenerator.generateCursor(firstItem, request.sortField(), request.direction().reverse())
            .map(cursor -> Optional.of(cursor.encode()));
    }
    
    private static class CursorGenerator<T> {
        private final PaginatedRepository<T> repository;
        
        CursorGenerator(PaginatedRepository<T> repository) {
            this.repository = repository;
        }
        
        Mono<Cursor> generateCursor(T entity, String sortField, SortDirection direction) {
            return Mono.zip(
                    repository.getFieldValue(entity, sortField),
                    repository.getId(entity)
                )
                .map(tuple -> Cursor.create(tuple.getT1(), tuple.getT2(), direction));
        }
    }
} 