package com.github.starter.modules.pokemon.endpoint;

import com.github.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.repository.EntriesRepository;
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

    @GetMapping("/list")
    public Flux<Pokemon> listEntries() {
        return entriesRepository.listEntries();
    }

}
