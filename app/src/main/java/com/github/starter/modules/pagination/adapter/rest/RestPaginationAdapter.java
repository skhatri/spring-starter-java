package com.github.starter.modules.pagination.adapter.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;

@Component
public class RestPaginationAdapter {
    
    public PaginationRequest toPaginationRequest(
        Integer limit,
        String nextToken,
        String previousToken,
        String sortField,
        String sortDirection
    ) {
        String cursor = nextToken != null ? nextToken : previousToken;
        SortDirection direction = parseSortDirection(sortDirection);
        
        if (nextToken == null && previousToken != null) {
            direction = direction.reverse();
        }
        
        return PaginationRequest.create(cursor, limit, sortField, direction);
    }
    
    public <T> PaginatedResponse<T> toPaginatedResponse(
        Page<T> page,
        PaginationRequest originalRequest,
        String baseUrl
    ) {
        PaginatedResponse.PaginationMetadata metadata = createMetadata(page);
        PaginatedResponse.PaginationLinks links = createLinks(page, originalRequest, baseUrl);
        
        return PaginatedResponse.create(page.content(), metadata, links);
    }
    
    public <T> PaginatedResponse<T> toPaginatedResponse(
        Page<T> page,
        PaginationRequest originalRequest
    ) {
        PaginatedResponse.PaginationMetadata metadata = createMetadata(page);
        return PaginatedResponse.create(page.content(), metadata);
    }
    
    private SortDirection parseSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return SortDirection.ASC;
        }
        
        return switch (sortDirection.toUpperCase()) {
            case "DESC", "DESCENDING" -> SortDirection.DESC;
            default -> SortDirection.ASC;
        };
    }
    
    private <T> PaginatedResponse.PaginationMetadata createMetadata(Page<T> page) {
        return PaginatedResponse.PaginationMetadata.create(
            page.pageSize(),
            page.totalCount(),
            page.hasNext(),
            page.hasPrevious(),
            page.nextCursor().orElse(null),
            page.previousCursor().orElse(null)
        );
    }
    
    private <T> PaginatedResponse.PaginationLinks createLinks(
        Page<T> page,
        PaginationRequest originalRequest,
        String baseUrl
    ) {
        String self = buildUrl(baseUrl, originalRequest.limit(), null, originalRequest.sortField());
        String first = buildUrl(baseUrl, originalRequest.limit(), null, originalRequest.sortField());
        
        String next = page.hasNext() && page.nextCursor().isPresent()
            ? buildUrl(baseUrl, originalRequest.limit(), page.nextCursor().get(), originalRequest.sortField())
            : null;
            
        String previous = page.hasPrevious() && page.previousCursor().isPresent()
            ? buildUrl(baseUrl, originalRequest.limit(), page.previousCursor().get(), originalRequest.sortField())
            : null;
        
        return PaginatedResponse.PaginationLinks.create(self, next, previous, first);
    }
    
    private String buildUrl(String baseUrl, int limit, String token, String sortField) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("limit", limit);
            
        if (sortField != null && !sortField.equals("id")) {
            builder.queryParam("sortField", sortField);
        }
        
        if (token != null) {
            builder.queryParam("nextToken", token);
        }
        
        return builder.build().toUriString();
    }
} 