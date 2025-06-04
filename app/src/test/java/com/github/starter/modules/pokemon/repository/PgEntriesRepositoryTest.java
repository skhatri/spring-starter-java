package com.github.starter.modules.pokemon.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;

import com.github.starter.PostgreSQLTestBase;
import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("PgEntriesRepository Tests")
@Import(PgEntriesRepositoryTest.TestConfig.class)
@TestPropertySource(properties = {
    "core.db=pg"
})
class PgEntriesRepositoryTest extends PostgreSQLTestBase {

    @Autowired
    private PgEntriesRepository repository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PgEntriesRepository pgEntriesRepository(DatabaseClient databaseClient) {
            return new PgEntriesRepository(databaseClient);
        }
    }

    @Test
    @DisplayName("Should find Pokemon by exact name")
    void shouldFindPokemonByExactName() {
        Mono<Pokemon> result = repository.findByName("Pikachu");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Pikachu", pokemon.getName());
                assertEquals("Electric", pokemon.getPrimaryType());
                assertNull(pokemon.getSecondaryType());
                assertEquals(25, pokemon.getPokedex());
                assertEquals(1, pokemon.getGeneration());
                assertEquals("kanto", pokemon.getRegion());
                assertTrue(pokemon.getTotal() > 0);
                assertTrue(pokemon.getHp() > 0);
                assertTrue(pokemon.getAttack() > 0);
                assertTrue(pokemon.getDefence() > 0);
                assertTrue(pokemon.getSpAttack() > 0);
                assertTrue(pokemon.getSpDefence() > 0);
                assertTrue(pokemon.getSpeed() > 0);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find Pokemon with dual type")
    void shouldFindPokemonWithDualType() {
        Mono<Pokemon> result = repository.findByName("Bulbasaur");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Bulbasaur", pokemon.getName());
                assertEquals("Grass", pokemon.getPrimaryType());
                assertEquals("Poison", pokemon.getSecondaryType());
                assertEquals(1, pokemon.getPokedex());
                assertEquals(1, pokemon.getGeneration());
                assertEquals("kanto", pokemon.getRegion());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when Pokemon not found")
    void shouldReturnEmptyWhenPokemonNotFound() {
        Mono<Pokemon> result = repository.findByName("NonExistentPokemon");

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle Pokemon names with special characters")
    void shouldHandlePokemonNamesWithSpecialCharacters() {
        Mono<Pokemon> result = repository.findByName("Nidoran♀");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Nidoran♀", pokemon.getName());
                assertEquals("Poison", pokemon.getPrimaryType());
                assertEquals(1, pokemon.getGeneration());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find effectiveness for single type")
    void shouldFindEffectivenessForSingleType() {
        Mono<Effectiveness> result = repository.findEffectiveness("fire", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("fire", effectiveness.getTypeName());
                assertNotNull(effectiveness.getNoEffect());
                assertNotNull(effectiveness.getDoubleResistant());
                assertNotNull(effectiveness.getNotVeryEffective());
                assertNotNull(effectiveness.getNeutral());
                assertNotNull(effectiveness.getEffective());
                assertNotNull(effectiveness.getSuperEffective());
                
                assertTrue(effectiveness.getNotVeryEffective().contains("fire"));
                assertTrue(effectiveness.getNotVeryEffective().contains("grass"));
                assertTrue(effectiveness.getEffective().contains("water"));
                assertTrue(effectiveness.getEffective().contains("ground"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find effectiveness for dual type")
    void shouldFindEffectivenessForDualType() {
        Mono<Effectiveness> result = repository.findEffectiveness("bug", "electric");

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("bug electric", effectiveness.getTypeName());
                assertNotNull(effectiveness.getNoEffect());
                assertNotNull(effectiveness.getDoubleResistant());
                assertNotNull(effectiveness.getNotVeryEffective());
                assertNotNull(effectiveness.getNeutral());
                assertNotNull(effectiveness.getEffective());
                assertNotNull(effectiveness.getSuperEffective());
                
                assertTrue(effectiveness.getNotVeryEffective().contains("electric"));
                assertTrue(effectiveness.getNotVeryEffective().contains("grass"));
                assertTrue(effectiveness.getEffective().contains("fire"));
                assertTrue(effectiveness.getEffective().contains("flying"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when effectiveness not found")
    void shouldReturnEmptyWhenEffectivenessNotFound() {
        Mono<Effectiveness> result = repository.findEffectiveness("nonexistent", null);

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null secondary type correctly")
    void shouldHandleNullSecondaryTypeCorrectly() {
        Mono<Effectiveness> result = repository.findEffectiveness("water", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("water", effectiveness.getTypeName());
                assertTrue(effectiveness.getNotVeryEffective().contains("fire"));
                assertTrue(effectiveness.getNotVeryEffective().contains("water"));
                assertTrue(effectiveness.getEffective().contains("electric"));
                assertTrue(effectiveness.getEffective().contains("grass"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find electric type effectiveness")
    void shouldFindElectricTypeEffectiveness() {
        Mono<Effectiveness> result = repository.findEffectiveness("electric", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("electric", effectiveness.getTypeName());
                assertTrue(effectiveness.getNotVeryEffective().contains("electric"));
                assertTrue(effectiveness.getNotVeryEffective().contains("flying"));
                assertTrue(effectiveness.getEffective().contains("ground"));
                assertTrue(effectiveness.getNoEffect().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find normal type effectiveness with no effect types")
    void shouldFindNormalTypeEffectivenessWithNoEffectTypes() {
        Mono<Effectiveness> result = repository.findEffectiveness("normal", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("normal", effectiveness.getTypeName());
                assertTrue(effectiveness.getNoEffect().contains("ghost"));
                assertTrue(effectiveness.getNotVeryEffective().contains("rock"));
                assertTrue(effectiveness.getNotVeryEffective().contains("steel"));
                assertTrue(effectiveness.getEffective().contains("fighting"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle case sensitivity in Pokemon names")
    void shouldHandleCaseSensitivityInPokemonNames() {
        Mono<Pokemon> result = repository.findByName("pikachu");

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle case sensitivity in type names")
    void shouldHandleCaseSensitivityInTypeNames() {
        Mono<Effectiveness> result = repository.findEffectiveness("Fire", null);

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find legendary Pokemon")
    void shouldFindLegendaryPokemon() {
        Mono<Pokemon> result = repository.findByName("Articuno");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Articuno", pokemon.getName());
                assertEquals("Ice", pokemon.getPrimaryType());
                assertEquals("Flying", pokemon.getSecondaryType());
                assertEquals("True", pokemon.getLegendary());
                assertEquals(1, pokemon.getGeneration());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find Pokemon from different generations")
    void shouldFindPokemonFromDifferentGenerations() {
        Mono<Pokemon> gen2Result = repository.findByName("Chikorita");

        StepVerifier.create(gen2Result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Chikorita", pokemon.getName());
                assertEquals(2, pokemon.getGeneration());
                assertEquals("johto", pokemon.getRegion());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should verify repository is properly configured")
    void shouldVerifyRepositoryIsProperlyConfigured() {
        assertNotNull(repository);
        
        Mono<Pokemon> testResult = repository.findByName("Bulbasaur");
        assertNotNull(testResult);
        
        Pokemon pokemon = testResult.block();
        assertNotNull(pokemon);
        assertEquals("Bulbasaur", pokemon.getName());
    }
} 