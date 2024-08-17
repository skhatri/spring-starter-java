package com.github.starter.repository;

import com.github.pokemon.model.Pokemon;
import io.r2dbc.spi.Readable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.function.Function;

@Repository
public class EntriesRepository {

    private final DatabaseClient client;

    @Autowired
    public EntriesRepository(DatabaseClient client) {
        this.client = client;
    }

    public Flux<Pokemon> listEntries() {
        return client.sql("SELECT * FROM pokedex.entries").map(new Function<Readable, Pokemon>() {
            @Override
            public Pokemon apply(Readable kv) {
                return new Pokemon((String) kv.get("name"),
                        (Integer) kv.get("base_stat"),
                        (String) kv.get("primary_type"),
                        (String) kv.get("secondary_type"),
                        (String) kv.get("location"),
                        (Boolean) kv.get("legendary"),
                        (String[]) kv.get("weakness"),
                        (BigDecimal) kv.get("height"),
                        (BigDecimal) kv.get("weight"));
            }
        }).all();
    }
}
