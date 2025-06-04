package com.github.starter.modules.pokemon.model;

import java.util.List;

public record PokemonPage(
    List<Pokemon> content,
    String nextCursor,
    String previousCursor,
    boolean hasNext,
    boolean hasPrevious,
    int totalCount,
    int pageSize
) {
    
    public PokemonPage {
        if (content == null) {
            content = List.of();
        }
        if (pageSize < 0) {
            pageSize = 0;
        }
        if (totalCount < 0) {
            totalCount = 0;
        }
    }
    
    public static PokemonPage fromPage(com.github.starter.modules.pagination.model.Page<Pokemon> page) {
        return new PokemonPage(
            page.content(),
            page.nextCursor().orElse(null),
            page.previousCursor().orElse(null),
            page.hasNext(),
            page.hasPrevious(),
            (int) page.totalCount(),
            page.pageSize()
        );
    }
} 