package com.github.starter.modules.pagination.adapter.rest;

import java.util.List;
import java.util.Optional;

public record PaginatedResponse<T>(
    List<T> data,
    PaginationMetadata pagination,
    Optional<PaginationLinks> links
) {
    
    public PaginatedResponse {
        if (data == null) {
            data = List.of();
        }
        if (pagination == null) {
            throw new IllegalArgumentException("Pagination metadata cannot be null");
        }
        if (links == null) {
            links = Optional.empty();
        }
    }
    
    public static <T> PaginatedResponse<T> create(
        List<T> data,
        PaginationMetadata pagination,
        PaginationLinks links
    ) {
        return new PaginatedResponse<>(data, pagination, Optional.ofNullable(links));
    }
    
    public static <T> PaginatedResponse<T> create(
        List<T> data,
        PaginationMetadata pagination
    ) {
        return new PaginatedResponse<>(data, pagination, Optional.empty());
    }
    
    public record PaginationMetadata(
        int limit,
        long total,
        boolean hasNext,
        boolean hasPrevious,
        Optional<String> nextToken,
        Optional<String> previousToken
    ) {
        
        public PaginationMetadata {
            if (limit < 0) {
                limit = 0;
            }
            if (total < 0) {
                total = 0;
            }
            if (nextToken == null) {
                nextToken = Optional.empty();
            }
            if (previousToken == null) {
                previousToken = Optional.empty();
            }
        }
        
        public static PaginationMetadata create(
            int limit,
            long total,
            boolean hasNext,
            boolean hasPrevious,
            String nextToken,
            String previousToken
        ) {
            return new PaginationMetadata(
                limit,
                total,
                hasNext,
                hasPrevious,
                Optional.ofNullable(nextToken),
                Optional.ofNullable(previousToken)
            );
        }
    }
    
    public record PaginationLinks(
        String self,
        Optional<String> next,
        Optional<String> previous,
        String first
    ) {
        
        public PaginationLinks {
            if (self == null || self.isBlank()) {
                throw new IllegalArgumentException("Self link cannot be null or blank");
            }
            if (first == null || first.isBlank()) {
                throw new IllegalArgumentException("First link cannot be null or blank");
            }
            if (next == null) {
                next = Optional.empty();
            }
            if (previous == null) {
                previous = Optional.empty();
            }
        }
        
        public static PaginationLinks create(
            String self,
            String next,
            String previous,
            String first
        ) {
            return new PaginationLinks(
                self,
                Optional.ofNullable(next),
                Optional.ofNullable(previous),
                first
            );
        }
    }
} 