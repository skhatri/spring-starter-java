package com.github.starter.modules.pagination.repository;

import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.SortDirection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaginatedRepository<T> {
    
    Flux<T> findWithCursor(
        Cursor cursor,
        int limit,
        String sortField,
        SortDirection direction
    );
    
    Flux<T> findFirst(
        int limit,
        String sortField,
        SortDirection direction
    );
    
    Mono<Long> count();
    
    Mono<Object> getFieldValue(T entity, String fieldName);
    
    Mono<Long> getId(T entity);
} 