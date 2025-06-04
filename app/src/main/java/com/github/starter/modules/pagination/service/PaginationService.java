package com.github.starter.modules.pagination.service;

import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;

import reactor.core.publisher.Mono;

public interface PaginationService<T> {
    
    Mono<Page<T>> findPage(PaginationRequest request);
    
    Mono<Long> getTotalCount();
    
    Mono<Page<T>> findFirstPage(int limit, String sortField);
    
    Mono<Page<T>> findNextPage(String cursor, int limit);
    
    Mono<Page<T>> findPreviousPage(String cursor, int limit);
} 