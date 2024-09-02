package com.github.starter.modules.pokemon.repository;

import com.github.pokemon.model.Pokemon;
import reactor.core.publisher.Flux;

public interface EntriesRepository {
    Flux<Pokemon> listEntries();
}
