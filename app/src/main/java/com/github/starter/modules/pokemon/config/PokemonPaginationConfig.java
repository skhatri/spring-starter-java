package com.github.starter.modules.pokemon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.starter.modules.pagination.service.DefaultPaginationService;
import com.github.starter.modules.pagination.service.PaginationService;
import com.github.starter.modules.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.repository.PaginatedPokemonRepository;

@Configuration
public class PokemonPaginationConfig {
    
    @Bean
    public PaginationService<Pokemon> pokemonPaginationService(
        PaginatedPokemonRepository repository
    ) {
        return new DefaultPaginationService<>(repository);
    }
} 