package com.github.starter.modules.pokemon.repository;
import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;

import reactor.core.publisher.Mono;

public interface EntriesRepository {
    Mono<Effectiveness> findEffectiveness(String primaryType, String secondaryType);
    
    Mono<Pokemon> findByName(String name);
}
