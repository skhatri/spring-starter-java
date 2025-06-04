package com.github.starter.modules.pagination.model;

import java.util.Optional;
import java.util.Set;

public record PaginationRequest(
    Optional<Cursor> cursor,
    Integer limit,
    SortDirection direction,
    String sortField
) {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.ASC;
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "name", "email", "createdAt", "status",
        "attack", "defence", "spAttack", "spDefence", 
        "primaryType", "secondaryType", "region", "legendary"
    );
    
    public PaginationRequest {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        if (direction == null) {
            direction = DEFAULT_DIRECTION;
        }
        if (sortField == null || sortField.isBlank()) {
            sortField = DEFAULT_SORT_FIELD;
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
        if (cursor == null) {
            cursor = Optional.empty();
        }
    }
    
    public static PaginationRequest defaultRequest() {
        return new PaginationRequest(
            Optional.empty(),
            DEFAULT_LIMIT,
            DEFAULT_DIRECTION,
            DEFAULT_SORT_FIELD
        );
    }
    
    public static PaginationRequest withLimit(int limit) {
        return new PaginationRequest(
            Optional.empty(),
            limit,
            DEFAULT_DIRECTION,
            DEFAULT_SORT_FIELD
        );
    }
    
    public static PaginationRequest withCursor(String cursorString) {
        Optional<Cursor> cursor = Cursor.decode(cursorString);
        return new PaginationRequest(
            cursor,
            DEFAULT_LIMIT,
            cursor.map(Cursor::direction).orElse(DEFAULT_DIRECTION),
            DEFAULT_SORT_FIELD
        );
    }
    
    public static PaginationRequest create(
        String cursorString,
        Integer limit,
        String sortField,
        SortDirection direction
    ) {
        return new PaginationRequest(
            Optional.ofNullable(cursorString).flatMap(Cursor::decode),
            limit,
            direction,
            sortField
        );
    }
    
    public PaginationRequest withCursor(Cursor newCursor) {
        return new PaginationRequest(
            Optional.ofNullable(newCursor),
            limit,
            direction,
            sortField
        );
    }
    
    public PaginationRequest withSortField(String newSortField) {
        return new PaginationRequest(
            cursor,
            limit,
            direction,
            newSortField
        );
    }
    
    public PaginationRequest withDirection(SortDirection newDirection) {
        return new PaginationRequest(
            cursor,
            limit,
            newDirection,
            sortField
        );
    }
    
    public boolean hasCursor() {
        return cursor.isPresent();
    }
    
    public boolean isForwardPagination() {
        return direction == SortDirection.ASC;
    }
    
    public boolean isBackwardPagination() {
        return direction == SortDirection.DESC;
    }
    
    public PaginationRequest reverse() {
        return withDirection(direction.reverse());
    }
} 