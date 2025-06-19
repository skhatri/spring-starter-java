package com.github.starter.modules.pagination.adapter.graphql;

import java.util.Locale;
import org.springframework.stereotype.Component;

import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;

@Component
public class GraphQLPaginationAdapter {
    
    public PaginationRequest toPaginationRequest(
        Integer limit,
        String cursor,
        String previousCursor,
        String sortField,
        String sortDirection
    ) {
        String activeCursor = cursor != null ? cursor : previousCursor;
        SortDirection direction = parseSortDirection(sortDirection);
        
        if (cursor == null && previousCursor != null) {
            direction = direction.reverse();
        }
        
        return PaginationRequest.create(activeCursor, limit, sortField, direction);
    }
    
    public PaginationRequest toPaginationRequest(
        Integer first,
        String after,
        Integer last,
        String before
    ) {
        Integer limit = first != null ? first : last;
        String cursor = after != null ? after : before;
        SortDirection direction = after != null || before == null ? SortDirection.ASC : SortDirection.DESC;
        
        return PaginationRequest.create(cursor, limit, "id", direction);
    }
    
    private SortDirection parseSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return SortDirection.ASC;
        }
        
        return switch (sortDirection.toUpperCase(Locale.ROOT)) {
            case "DESC", "DESCENDING" -> SortDirection.DESC;
            default -> SortDirection.ASC;
        };
    }
} 