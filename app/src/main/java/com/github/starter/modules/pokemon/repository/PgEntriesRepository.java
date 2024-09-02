package com.github.starter.modules.pokemon.repository;

import com.github.pokemon.model.Pokemon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "pg")
@Repository
public class PgEntriesRepository implements EntriesRepository {

    private final DatabaseClient client;

    @Autowired
    public PgEntriesRepository(DatabaseClient client) {
        this.client = client;
    }

    public Flux<Pokemon> listEntries() {
        return client.sql("SELECT * FROM app.pokemons").map(kv -> new Pokemon((String) kv.get("name"),
                (Integer) kv.get("base_stat"),
                (String) kv.get("primary_type"),
                (String) kv.get("secondary_type"),
                (String) kv.get("location"),
                (Boolean) kv.get("legendary"),
                (String[]) kv.get("weakness"),
                (BigDecimal) kv.get("height"),
                (BigDecimal) kv.get("weight"))).all();
    }
}
