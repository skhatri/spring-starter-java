package com.github.starter.modules.pokemon.model;

import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
            content = Collections.emptyList();
        } else {
            content = Collections.unmodifiableList(List.copyOf(content));
        }
        if (pageSize < 0) {
            pageSize = 0;
        }
        if (totalCount < 0) {
            totalCount = 0;
        }
    }
    
    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Field is already wrapped in unmodifiable list in constructor")
    public List<Pokemon> content() {
        return content;
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