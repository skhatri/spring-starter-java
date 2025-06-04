package com.github.starter.modules.pagination.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record Page<T>(
    List<T> content,
    Optional<String> nextCursor,
    Optional<String> previousCursor,
    boolean hasNext,
    boolean hasPrevious,
    long totalCount,
    int pageSize
) {
    
    public Page {
        if (content == null) {
            content = List.of();
            pageSize = 0;
        }
        if (nextCursor == null) {
            nextCursor = Optional.empty();
        }
        if (previousCursor == null) {
            previousCursor = Optional.empty();
        }
        if (pageSize < 0) {
            pageSize = 0;
        }
        if (totalCount < 0) {
            totalCount = 0;
        }
    }
    
    public static <T> Page<T> empty() {
        return new Page<>(
            List.of(),
            Optional.empty(),
            Optional.empty(),
            false,
            false,
            0L,
            0
        );
    }
    
    public static <T> Page<T> single(List<T> content, long totalCount) {
        return new Page<>(
            content,
            Optional.empty(),
            Optional.empty(),
            false,
            false,
            totalCount,
            content.size()
        );
    }
    
    public static <T> Page<T> create(
        List<T> content,
        String nextCursor,
        String previousCursor,
        boolean hasNext,
        boolean hasPrevious,
        long totalCount
    ) {
        return new Page<>(
            content,
            Optional.ofNullable(nextCursor),
            Optional.ofNullable(previousCursor),
            hasNext,
            hasPrevious,
            totalCount,
            content.size()
        );
    }
    
    public <U> Page<U> map(Function<T, U> mapper) {
        List<U> mappedContent = content.stream()
            .map(mapper)
            .toList();
            
        return new Page<>(
            mappedContent,
            nextCursor,
            previousCursor,
            hasNext,
            hasPrevious,
            totalCount,
            pageSize
        );
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    public boolean isNotEmpty() {
        return !isEmpty();
    }
    
    public int size() {
        return content.size();
    }
    
    public boolean isFirstPage() {
        return !hasPrevious;
    }
    
    public boolean isLastPage() {
        return !hasNext;
    }
    
    public boolean isSinglePage() {
        return isFirstPage() && isLastPage();
    }
    
    public Optional<T> getFirst() {
        return content.isEmpty() ? Optional.empty() : Optional.of(content.get(0));
    }
    
    public Optional<T> getLast() {
        return content.isEmpty() ? Optional.empty() : Optional.of(content.get(content.size() - 1));
    }
    
    public Page<T> withTotalCount(long newTotalCount) {
        return new Page<>(
            content,
            nextCursor,
            previousCursor,
            hasNext,
            hasPrevious,
            newTotalCount,
            pageSize
        );
    }
} 