package com.github.starter.modules.pokemon.repository;

import com.github.pokemon.model.Pokemon;
import com.github.starter.config.duck.DataClient;
import com.github.starter.exception.Exceptions;
import org.duckdb.DuckDBArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.sql.SQLException;

@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "duckdb")
@Repository
public class DuckEntriesRepository implements EntriesRepository {

    private final DataClient client;

    @Autowired
    public DuckEntriesRepository(DataClient client) {
        this.client = client;
    }

    public Flux<Pokemon> listEntries() {
        return client.sql("SELECT * FROM pokemons").map(kv -> {
            try {
                Object[] wo = (Object[]) ((DuckDBArray) kv.get("weakness")).getArray();
                String[] weakness = new String[wo.length];
                for (int i = 0; i < wo.length; i++) {
                    weakness[i] = String.valueOf(wo[i]);
                }
                return new Pokemon((String) kv.get("name"),
                        (Integer) kv.get("base_stat"),
                        (String) kv.get("primary_type"),
                        (String) kv.get("secondary_type"),
                        (String) kv.get("location"),
                        (Boolean) kv.get("legendary"),
                        weakness,
                        (BigDecimal) kv.get("height"),
                        (BigDecimal) kv.get("weight"));
            } catch (SQLException sqle) {
                throw Exceptions.dbException(sqle);
            }
        });
    }
}
