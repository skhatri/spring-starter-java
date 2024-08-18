package com.github.starter.modules.pokemon.endpoint;

import com.github.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.repository.EntriesRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("pokemon")
public class EntriesEndpoint {

    private final EntriesRepository entriesRepository;

    public EntriesEndpoint(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }

    @QueryMapping
    @GetMapping("/list")
    public Flux<Pokemon> getPokemonList() {
        return entriesRepository.listEntries();
    }

}
