package com.github.starter.modules.pokemon.service;

import java.util.List;

import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;

import reactor.core.publisher.Mono;

public interface EntriesService {
    
    Mono<Effectiveness> findEffectiveness(String primaryType, String secondaryType);
    
    Mono<Pokemon> findByName(String name);

    Mono<Pokemon> findByNameWithEffectiveness(String name);
    
    Mono<Effectiveness> getEffectivenessForPokemon(Pokemon pokemon);
    
    Mono<List<Pokemon>> enrichPokemonListWithEffectiveness(List<Pokemon> pokemonList);
} 