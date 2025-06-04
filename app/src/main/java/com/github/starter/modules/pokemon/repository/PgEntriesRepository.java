package com.github.starter.modules.pokemon.repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;

import reactor.core.publisher.Mono;
@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "pg")
@Repository
public class PgEntriesRepository implements EntriesRepository {
    private static final Logger logger = LoggerFactory.getLogger(PgEntriesRepository.class);
    
    private final DatabaseClient client;
    @Autowired
    public PgEntriesRepository(DatabaseClient client) {
        this.client = client;
    }
    
    @Override
    public Mono<Effectiveness> findEffectiveness(String primaryType, String secondaryType) {
        if (secondaryType == null) {
            return client.sql("SELECT * FROM app.effectiveness WHERE type_name = :type")
                    .bind("type", primaryType)
                    .map(Effectiveness::fromReadable)
                    .one();
        } else {
            String combinedType = primaryType + " " + secondaryType;
            return client.sql("SELECT * FROM app.effectiveness WHERE type_name = :type")
                    .bind("type", combinedType)
                    .map(Effectiveness::fromReadable)
                    .one();
        }
    }
    
    @Override
    public Mono<Pokemon> findByName(String name) {
        return client.sql("SELECT * FROM app.pokemons WHERE name = :name")
                .bind("name", name)
                .map(Pokemon::fromReadable)
                .one();
    }
}
