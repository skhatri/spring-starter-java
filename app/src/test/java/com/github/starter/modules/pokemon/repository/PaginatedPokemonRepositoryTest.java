package com.github.starter.modules.pokemon.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

import com.github.starter.PostgreSQLTestBase;
import com.github.starter.modules.pagination.model.Cursor;
import com.github.starter.modules.pagination.model.SortDirection;
import com.github.starter.modules.pokemon.model.Pokemon;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("PaginatedPokemonRepository Tests")
@Import(PaginatedPokemonRepositoryTest.TestConfig.class)
class PaginatedPokemonRepositoryTest extends PostgreSQLTestBase {

    @Autowired
    private PaginatedPokemonRepository repository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PaginatedPokemonRepository paginatedPokemonRepository(DatabaseClient databaseClient) {
            return new PaginatedPokemonRepository(databaseClient);
        }
    }

    @Test
    @DisplayName("Should find first page with ascending sort by name")
    void shouldFindFirstPageWithAscendingSortByName() {
        Flux<Pokemon> result = repository.findFirst(5, "name", SortDirection.ASC);

        StepVerifier.create(result.collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertEquals(5, pokemons.size());
                
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    String current = pokemons.get(i).getName();
                    String next = pokemons.get(i + 1).getName();
                    assertTrue(current.compareTo(next) <= 0, 
                        "Pokemon should be sorted by name ascending: " + current + " vs " + next);
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find first page with descending sort by attack")
    void shouldFindFirstPageWithDescendingSortByAttack() {
        Flux<Pokemon> result = repository.findFirst(3, "attack", SortDirection.DESC);

        StepVerifier.create(result.collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertEquals(3, pokemons.size());
                
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    int current = pokemons.get(i).getAttack();
                    int next = pokemons.get(i + 1).getAttack();
                    assertTrue(current >= next, 
                        "Pokemon should be sorted by attack descending: " + current + " vs " + next);
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find first page with sort by pokedex ID")
    void shouldFindFirstPageWithSortByPokedexId() {
        Flux<Pokemon> result = repository.findFirst(10, "id", SortDirection.ASC);

        StepVerifier.create(result.collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertEquals(10, pokemons.size());
                
                assertEquals(1, pokemons.get(0).getPokedex());
                assertEquals("Bulbasaur", pokemons.get(0).getName());
                
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    int current = pokemons.get(i).getPokedex();
                    int next = pokemons.get(i + 1).getPokedex();
                    assertTrue(current <= next, 
                        "Pokemon should be sorted by pokedex ID ascending: " + current + " vs " + next);
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find with cursor pagination ascending")
    void shouldFindWithCursorPaginationAscending() {
        List<Pokemon> firstPage = repository.findFirst(3, "name", SortDirection.ASC)
            .collectList()
            .block();
        
        assertNotNull(firstPage);
        assertEquals(3, firstPage.size());
        
        Pokemon lastPokemon = firstPage.get(2);
        Cursor cursor = Cursor.create(lastPokemon.getName(), (long) lastPokemon.getPokedex(), SortDirection.ASC);
        
        Flux<Pokemon> result = repository.findWithCursor(cursor, 3, "name", SortDirection.ASC);

        StepVerifier.create(result.collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertTrue(pokemons.size() <= 3);
                
                if (!pokemons.isEmpty()) {
                    String firstNameInSecondPage = pokemons.get(0).getName();
                    assertTrue(firstNameInSecondPage.compareTo(lastPokemon.getName()) > 0 ||
                        (firstNameInSecondPage.equals(lastPokemon.getName()) && 
                         pokemons.get(0).getPokedex() > lastPokemon.getPokedex()),
                        "Second page should start after the cursor");
                    
                    for (int i = 0; i < pokemons.size() - 1; i++) {
                        String current = pokemons.get(i).getName();
                        String next = pokemons.get(i + 1).getName();
                        assertTrue(current.compareTo(next) <= 0, 
                            "Pokemon should be sorted by name ascending");
                    }
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find with cursor pagination descending")
    void shouldFindWithCursorPaginationDescending() {
        List<Pokemon> firstPage = repository.findFirst(3, "attack", SortDirection.DESC)
            .collectList()
            .block();
        
        assertNotNull(firstPage);
        assertEquals(3, firstPage.size());
        
        Pokemon lastPokemon = firstPage.get(2);
        Cursor cursor = Cursor.create(lastPokemon.getAttack(), (long) lastPokemon.getPokedex(), SortDirection.DESC);
        
        Flux<Pokemon> result = repository.findWithCursor(cursor, 3, "attack", SortDirection.DESC);

        StepVerifier.create(result.collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertTrue(pokemons.size() <= 3);
                
                if (!pokemons.isEmpty()) {
                    for (Pokemon pokemon : pokemons) {
                        assertFalse(pokemon.getPokedex() == lastPokemon.getPokedex(),
                            "Second page should not contain the cursor Pokemon itself");
                    }
                    
                    for (int i = 0; i < pokemons.size() - 1; i++) {
                        int current = pokemons.get(i).getAttack();
                        int next = pokemons.get(i + 1).getAttack();
                        assertTrue(current >= next, 
                            "Pokemon should be sorted by attack descending");
                    }
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should count total Pokemon")
    void shouldCountTotalPokemon() {
        Mono<Long> result = repository.count();

        StepVerifier.create(result)
            .assertNext(count -> {
                assertNotNull(count);
                assertEquals(808L, count, "Should have exactly 808 Pokemon in database");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get Pokemon ID correctly")
    void shouldGetPokemonIdCorrectly() {
        Pokemon pikachu = repository.findFirst(100, "id", SortDirection.ASC)
            .filter(p -> "Pikachu".equals(p.getName()))
            .blockFirst();
        
        assertNotNull(pikachu, "Pikachu should be found in database");
        
        final Pokemon finalPikachu = pikachu;
        Mono<Long> result = repository.getId(finalPikachu);

        StepVerifier.create(result)
            .assertNext(id -> {
                assertNotNull(id);
                assertEquals(25L, id, "Pikachu's ID should be 25");
                assertEquals((long) finalPikachu.getPokedex(), id.longValue());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should extract field values correctly")
    void shouldExtractFieldValuesCorrectly() {
        Pokemon bulbasaur = repository.findFirst(1, "id", SortDirection.ASC)
            .blockFirst();
        
        assertNotNull(bulbasaur, "Bulbasaur should be the first Pokemon");
        assertEquals("Bulbasaur", bulbasaur.getName());

        StepVerifier.create(repository.getFieldValue(bulbasaur, "name"))
            .assertNext(value -> assertEquals("Bulbasaur", value))
            .verifyComplete();

        StepVerifier.create(repository.getFieldValue(bulbasaur, "id"))
            .assertNext(value -> assertEquals(1, value))
            .verifyComplete();

        StepVerifier.create(repository.getFieldValue(bulbasaur, "primaryType"))
            .assertNext(value -> assertEquals("Grass", value))
            .verifyComplete();

        StepVerifier.create(repository.getFieldValue(bulbasaur, "secondaryType"))
            .assertNext(value -> assertEquals("Poison", value))
            .verifyComplete();

        StepVerifier.create(repository.getFieldValue(bulbasaur, "attack"))
            .assertNext(value -> assertEquals(49, value))
            .verifyComplete();

        StepVerifier.create(repository.getFieldValue(bulbasaur, "defence"))
            .assertNext(value -> assertEquals(49, value))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle field extraction errors gracefully")
    void shouldHandleFieldExtractionErrorsGracefully() {
        Pokemon pokemon = repository.findFirst(1, "id", SortDirection.ASC)
            .blockFirst();
        
        assertNotNull(pokemon);

        StepVerifier.create(repository.getFieldValue(pokemon, "nonExistentField"))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    @DisplayName("Should sort by different fields correctly")
    void shouldSortByDifferentFieldsCorrectly() {
        StepVerifier.create(repository.findFirst(5, "defence", SortDirection.ASC).collectList())
            .assertNext(pokemons -> {
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    int current = pokemons.get(i).getDefence();
                    int next = pokemons.get(i + 1).getDefence();
                    assertTrue(current <= next, "Should be sorted by defence ascending");
                }
            })
            .verifyComplete();

        StepVerifier.create(repository.findFirst(5, "spAttack", SortDirection.DESC).collectList())
            .assertNext(pokemons -> {
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    int current = pokemons.get(i).getSpAttack();
                    int next = pokemons.get(i + 1).getSpAttack();
                    assertTrue(current >= next, "Should be sorted by spAttack descending");
                }
            })
            .verifyComplete();

        StepVerifier.create(repository.findFirst(5, "region", SortDirection.ASC).collectList())
            .assertNext(pokemons -> {
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    String current = pokemons.get(i).getRegion();
                    String next = pokemons.get(i + 1).getRegion();
                    assertTrue(current.compareTo(next) <= 0, "Should be sorted by region ascending");
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        Cursor impossibleCursor = Cursor.create("ZZZZZZZ", 99999L, SortDirection.ASC);
        
        StepVerifier.create(repository.findWithCursor(impossibleCursor, 10, "name", SortDirection.ASC))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle large limit values")
    void shouldHandleLargeLimitValues() {
        Flux<Pokemon> result = repository.findFirst(1000, "id", SortDirection.ASC);

        StepVerifier.create(result.count())
            .assertNext(count -> {
                assertTrue(count <= 808L, "Should not return more Pokemon than exist in database");
                assertTrue(count > 0, "Should return some Pokemon");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle field mapping correctly")
    void shouldHandleFieldMappingCorrectly() {
        StepVerifier.create(repository.findFirst(3, "primaryType", SortDirection.ASC).collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertEquals(3, pokemons.size());
                
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    String current = pokemons.get(i).getPrimaryType();
                    String next = pokemons.get(i + 1).getPrimaryType();
                    assertTrue(current.compareTo(next) <= 0, 
                        "Should be sorted by primary type ascending");
                }
            })
            .verifyComplete();

        StepVerifier.create(repository.findFirst(3, "legendary", SortDirection.ASC).collectList())
            .assertNext(pokemons -> {
                assertNotNull(pokemons);
                assertEquals(3, pokemons.size());
                
                for (int i = 0; i < pokemons.size() - 1; i++) {
                    String current = pokemons.get(i).getLegendary();
                    String next = pokemons.get(i + 1).getLegendary();
                    assertTrue(current.compareTo(next) <= 0, 
                        "Should be sorted by legendary status ascending");
                }
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should maintain consistent ordering with secondary sort")
    void shouldMaintainConsistentOrderingWithSecondarySort() {
        List<Pokemon> firstCall = repository.findFirst(20, "primaryType", SortDirection.ASC)
            .collectList()
            .block();
        
        List<Pokemon> secondCall = repository.findFirst(20, "primaryType", SortDirection.ASC)
            .collectList()
            .block();
        
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall.size(), secondCall.size());
        
        for (int i = 0; i < firstCall.size(); i++) {
            assertEquals(firstCall.get(i).getPokedex(), secondCall.get(i).getPokedex(),
                "Results should be consistent across calls due to secondary sort by pokedex");
        }
    }

    @Test
    @DisplayName("Should verify repository is properly configured")
    void shouldVerifyRepositoryIsProperlyConfigured() {
        assertNotNull(repository);
        
        Mono<Long> countResult = repository.count();
        assertNotNull(countResult);
        
        Long count = countResult.block();
        assertNotNull(count);
        assertTrue(count > 0, "Repository should be connected to database with data");
    }
} 