package com.github.starter.modules.pokemon.repository;

import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.SortDirection;
import com.github.starter.modules.pagination.repository.PaginatedRepository;
import com.github.starter.modules.pokemon.model.Pokemon;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;

@Repository
public class PaginatedPokemonRepository implements PaginatedRepository<Pokemon> {
    
    private final DatabaseClient databaseClient;
    
    private static final Map<String, String> FIELD_MAPPINGS = Map.of(
        "id", "pokedex",
        "name", "name",
        "attack", "attack",
        "defence", "defence", 
        "spAttack", "sp_attack",
        "spDefence", "sp_defence",
        "primaryType", "primary_type",
        "secondaryType", "secondary_type",
        "region", "region",
        "legendary", "legendary"
    );
    
    public PaginatedPokemonRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }
    
    @Override
    public Flux<Pokemon> findWithCursor(
        Cursor cursor,
        int limit,
        String sortField,
        SortDirection direction
    ) {
        String dbField = getDbFieldName(sortField);
        String sql = buildCursorQuery(dbField, direction);
        
        return databaseClient.sql(sql)
            .bind("lastValue", cursor.lastValue())
            .bind("lastId", cursor.lastId())
            .bind("limit", limit)
            .map(Pokemon::fromReadable)
            .all();
    }
    
    @Override
    public Flux<Pokemon> findFirst(
        int limit,
        String sortField,
        SortDirection direction
    ) {
        String dbField = getDbFieldName(sortField);
        String sql = """
            SELECT * FROM app.pokemons 
            ORDER BY %s %s, pokedex %s 
            LIMIT :limit
            """.formatted(dbField, direction.toSql(), direction.toSql());
            
        return databaseClient.sql(sql)
            .bind("limit", limit)
            .map(Pokemon::fromReadable)
            .all();
    }
    
    @Override
    public Mono<Long> count() {
        return databaseClient.sql("SELECT COUNT(*) FROM app.pokemons")
            .map(row -> row.get(0, Long.class))
            .one();
    }
    
    @Override
    public Mono<Object> getFieldValue(Pokemon entity, String fieldName) {
        return Mono.fromCallable(() -> extractFieldValue(entity, fieldName));
    }
    
    @Override
    public Mono<Long> getId(Pokemon entity) {
        return Mono.just((long) entity.getPokedex());
    }
    
    private String buildCursorQuery(String dbField, SortDirection direction) {
        String operator = direction == SortDirection.ASC ? ">" : "<";
        
        return """
            SELECT * FROM app.pokemons 
            WHERE (%s %s :lastValue) 
               OR (%s = :lastValue AND pokedex %s :lastId)
            ORDER BY %s %s, pokedex %s 
            LIMIT :limit
            """.formatted(
                dbField, operator,
                dbField, operator,
                dbField, direction.toSql(), direction.toSql()
            );
    }
    
    private String getDbFieldName(String fieldName) {
        return FIELD_MAPPINGS.getOrDefault(fieldName, fieldName);
    }
    
    private Object extractFieldValue(Pokemon pokemon, String fieldName) {
        try {
            String methodName = getMethodNameForField(fieldName);
            Method method = Pokemon.class.getMethod(methodName);
            return method.invoke(pokemon);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot extract field " + fieldName + " from Pokemon", e);
        }
    }
    
    private String getMethodNameForField(String fieldName) {
        return switch (fieldName) {
            case "id" -> "getPokedex";
            default -> "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        };
    }
} 