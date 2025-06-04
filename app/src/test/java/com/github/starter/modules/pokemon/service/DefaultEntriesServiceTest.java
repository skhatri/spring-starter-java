package com.github.starter.modules.pokemon.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.repository.EntriesRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultEntriesService Tests")
class DefaultEntriesServiceTest {

    @Mock
    private EntriesRepository entriesRepository;

    private DefaultEntriesService service;

    @BeforeEach
    void setUp() {
        service = new DefaultEntriesService(entriesRepository);
    }

    @Test
    @DisplayName("Should find single-type effectiveness successfully")
    void shouldFindSingleTypeEffectivenessSuccessfully() {
        Effectiveness fireEffectiveness = createFireEffectiveness();
        when(entriesRepository.findEffectiveness("fire", null))
            .thenReturn(Mono.just(fireEffectiveness));

        Mono<Effectiveness> result = service.findEffectiveness("Fire", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("fire", effectiveness.getTypeName());
                assertTrue(effectiveness.getEffective().contains("grass"));
                assertTrue(effectiveness.getNotVeryEffective().contains("fire"));
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("fire", null);
    }

    @Test
    @DisplayName("Should find single-type effectiveness with empty secondary type")
    void shouldFindSingleTypeEffectivenessWithEmptySecondaryType() {
        Effectiveness waterEffectiveness = createWaterEffectiveness();
        when(entriesRepository.findEffectiveness("water", null))
            .thenReturn(Mono.just(waterEffectiveness));

        Mono<Effectiveness> result = service.findEffectiveness("Water", "");

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("water", effectiveness.getTypeName());
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("water", null);
    }

    @Test
    @DisplayName("Should handle single-type effectiveness not found")
    void shouldHandleSingleTypeEffectivenessNotFound() {
        when(entriesRepository.findEffectiveness("unknown", null))
            .thenReturn(Mono.error(new RuntimeException("Not found")));

        Mono<Effectiveness> result = service.findEffectiveness("Unknown", null);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("Unknown", effectiveness.getTypeName());
                assertTrue(effectiveness.getNoEffect().isEmpty());
                assertTrue(effectiveness.getEffective().isEmpty());
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("unknown", null);
    }

    @Test
    @DisplayName("Should calculate dual-type effectiveness successfully")
    void shouldCalculateDualTypeEffectivenessSuccessfully() {
        Effectiveness grassEffectiveness = createGrassEffectiveness();
        Effectiveness poisonEffectiveness = createPoisonEffectiveness();

        when(entriesRepository.findEffectiveness("grass", null))
            .thenReturn(Mono.just(grassEffectiveness));
        when(entriesRepository.findEffectiveness("poison", null))
            .thenReturn(Mono.just(poisonEffectiveness));

        Mono<Effectiveness> result = service.findEffectiveness("Grass", "Poison");

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("grass / poison", effectiveness.getTypeName());
                
                assertNotNull(effectiveness.getNoEffect());
                assertNotNull(effectiveness.getDoubleResistant());
                assertNotNull(effectiveness.getNotVeryEffective());
                assertNotNull(effectiveness.getNeutral());
                assertNotNull(effectiveness.getEffective());
                assertNotNull(effectiveness.getSuperEffective());
                
                assertTrue(effectiveness.getSuperEffective().contains("psychic"),
                    "Psychic should be super effective (1.0 * 4.0 = 4.0)");
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("grass", null);
        verify(entriesRepository).findEffectiveness("poison", null);
    }

    @Test
    @DisplayName("Should handle dual-type effectiveness with one type missing")
    void shouldHandleDualTypeEffectivenessWithOneTypeMissing() {
        Effectiveness fireEffectiveness = createFireEffectiveness();

        when(entriesRepository.findEffectiveness("fire", null))
            .thenReturn(Mono.just(fireEffectiveness));
        when(entriesRepository.findEffectiveness("unknown", null))
            .thenReturn(Mono.error(new RuntimeException("Not found")));

        Mono<Effectiveness> result = service.findEffectiveness("Fire", "Unknown");

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("fire / Unknown", effectiveness.getTypeName());
                
                assertTrue(effectiveness.getEffective().contains("grass"),
                    "Should still have fire effectiveness against grass");
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("fire", null);
        verify(entriesRepository).findEffectiveness("unknown", null);
    }

    @Test
    @DisplayName("Should find Pokemon by name successfully")
    void shouldFindPokemonByNameSuccessfully() {
        Pokemon pikachu = createPikachu();
        when(entriesRepository.findByName("Pikachu"))
            .thenReturn(Mono.just(pikachu));

        Mono<Pokemon> result = service.findByName("Pikachu");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Pikachu", pokemon.getName());
                assertEquals("Electric", pokemon.getPrimaryType());
                assertNull(pokemon.getEffectiveness());
            })
            .verifyComplete();

        verify(entriesRepository).findByName("Pikachu");
    }

    @Test
    @DisplayName("Should handle Pokemon not found by name")
    void shouldHandlePokemonNotFoundByName() {
        when(entriesRepository.findByName("NonExistent"))
            .thenReturn(Mono.error(new RuntimeException("Pokemon not found")));

        Mono<Pokemon> result = service.findByName("NonExistent");

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(entriesRepository).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should find Pokemon by name with effectiveness")
    void shouldFindPokemonByNameWithEffectiveness() {
        Pokemon pikachu = createPikachu();
        Effectiveness electricEffectiveness = createElectricEffectiveness();

        when(entriesRepository.findByName("Pikachu"))
            .thenReturn(Mono.just(pikachu));
        when(entriesRepository.findEffectiveness("electric", null))
            .thenReturn(Mono.just(electricEffectiveness));

        Mono<Pokemon> result = service.findByNameWithEffectiveness("Pikachu");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Pikachu", pokemon.getName());
                assertEquals("Electric", pokemon.getPrimaryType());
                assertNotNull(pokemon.getEffectiveness());
                assertEquals("electric", pokemon.getEffectiveness().getTypeName());
            })
            .verifyComplete();

        verify(entriesRepository).findByName("Pikachu");
        verify(entriesRepository).findEffectiveness("electric", null);
    }

    @Test
    @DisplayName("Should find dual-type Pokemon by name with effectiveness")
    void shouldFindDualTypePokemonByNameWithEffectiveness() {
        Pokemon bulbasaur = createBulbasaur();
        Effectiveness grassEffectiveness = createGrassEffectiveness();
        Effectiveness poisonEffectiveness = createPoisonEffectiveness();

        when(entriesRepository.findByName("Bulbasaur"))
            .thenReturn(Mono.just(bulbasaur));
        when(entriesRepository.findEffectiveness("grass", null))
            .thenReturn(Mono.just(grassEffectiveness));
        when(entriesRepository.findEffectiveness("poison", null))
            .thenReturn(Mono.just(poisonEffectiveness));

        Mono<Pokemon> result = service.findByNameWithEffectiveness("Bulbasaur");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Bulbasaur", pokemon.getName());
                assertEquals("Grass", pokemon.getPrimaryType());
                assertEquals("Poison", pokemon.getSecondaryType());
                assertNotNull(pokemon.getEffectiveness());
                assertEquals("grass / poison", pokemon.getEffectiveness().getTypeName());
            })
            .verifyComplete();

        verify(entriesRepository).findByName("Bulbasaur");
        verify(entriesRepository).findEffectiveness("grass", null);
        verify(entriesRepository).findEffectiveness("poison", null);
    }

    @Test
    @DisplayName("Should get effectiveness for Pokemon without existing effectiveness")
    void shouldGetEffectivenessForPokemonWithoutExistingEffectiveness() {
        Pokemon charmander = createCharmander();
        Effectiveness fireEffectiveness = createFireEffectiveness();

        when(entriesRepository.findEffectiveness("fire", null))
            .thenReturn(Mono.just(fireEffectiveness));

        Mono<Effectiveness> result = service.getEffectivenessForPokemon(charmander);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("fire", effectiveness.getTypeName());
                assertTrue(effectiveness.getEffective().contains("grass"));
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("fire", null);
    }

    @Test
    @DisplayName("Should get effectiveness for Pokemon with existing effectiveness")
    void shouldGetEffectivenessForPokemonWithExistingEffectiveness() {
        Pokemon pokemonWithEffectiveness = createPikachu();
        Effectiveness existingEffectiveness = createElectricEffectiveness();
        pokemonWithEffectiveness.setEffectiveness(existingEffectiveness);

        Mono<Effectiveness> result = service.getEffectivenessForPokemon(pokemonWithEffectiveness);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("electric", effectiveness.getTypeName());
            })
            .verifyComplete();

        verifyNoInteractions(entriesRepository);
    }

    @Test
    @DisplayName("Should enrich Pokemon list with effectiveness")
    void shouldEnrichPokemonListWithEffectiveness() {
        List<Pokemon> pokemonList = Arrays.asList(
            createPikachu(),
            createCharmander(),
            createBulbasaur()
        );

        Effectiveness electricEffectiveness = createElectricEffectiveness();
        Effectiveness fireEffectiveness = createFireEffectiveness();
        Effectiveness grassEffectiveness = createGrassEffectiveness();
        Effectiveness poisonEffectiveness = createPoisonEffectiveness();

        when(entriesRepository.findEffectiveness("electric", null))
            .thenReturn(Mono.just(electricEffectiveness));
        when(entriesRepository.findEffectiveness("fire", null))
            .thenReturn(Mono.just(fireEffectiveness));
        when(entriesRepository.findEffectiveness("grass", null))
            .thenReturn(Mono.just(grassEffectiveness));
        when(entriesRepository.findEffectiveness("poison", null))
            .thenReturn(Mono.just(poisonEffectiveness));

        Mono<List<Pokemon>> result = service.enrichPokemonListWithEffectiveness(pokemonList);

        StepVerifier.create(result)
            .assertNext(enrichedList -> {
                assertNotNull(enrichedList);
                assertEquals(3, enrichedList.size());

                Pokemon pikachu = enrichedList.stream()
                    .filter(p -> "Pikachu".equals(p.getName()))
                    .findFirst().orElse(null);
                assertNotNull(pikachu);
                assertNotNull(pikachu.getEffectiveness());
                assertEquals("electric", pikachu.getEffectiveness().getTypeName());

                Pokemon charmander = enrichedList.stream()
                    .filter(p -> "Charmander".equals(p.getName()))
                    .findFirst().orElse(null);
                assertNotNull(charmander);
                assertNotNull(charmander.getEffectiveness());
                assertEquals("fire", charmander.getEffectiveness().getTypeName());

                Pokemon bulbasaur = enrichedList.stream()
                    .filter(p -> "Bulbasaur".equals(p.getName()))
                    .findFirst().orElse(null);
                assertNotNull(bulbasaur);
                assertNotNull(bulbasaur.getEffectiveness());
                assertEquals("grass / poison", bulbasaur.getEffectiveness().getTypeName());
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("electric", null);
        verify(entriesRepository).findEffectiveness("fire", null);
        verify(entriesRepository).findEffectiveness("grass", null);
        verify(entriesRepository).findEffectiveness("poison", null);
    }

    @Test
    @DisplayName("Should handle errors when enriching Pokemon list")
    void shouldHandleErrorsWhenEnrichingPokemonList() {
        List<Pokemon> pokemonList = Arrays.asList(
            createPikachu(),
            createUnknownTypePokemon()
        );

        Effectiveness electricEffectiveness = createElectricEffectiveness();

        when(entriesRepository.findEffectiveness("electric", null))
            .thenReturn(Mono.just(electricEffectiveness));
        when(entriesRepository.findEffectiveness("unknown", null))
            .thenReturn(Mono.error(new RuntimeException("Type not found")));

        Mono<List<Pokemon>> result = service.enrichPokemonListWithEffectiveness(pokemonList);

        StepVerifier.create(result)
            .assertNext(enrichedList -> {
                assertNotNull(enrichedList);
                assertEquals(2, enrichedList.size());

                Pokemon pikachu = enrichedList.stream()
                    .filter(p -> "Pikachu".equals(p.getName()))
                    .findFirst().orElse(null);
                assertNotNull(pikachu);
                assertNotNull(pikachu.getEffectiveness());

                Pokemon unknownPokemon = enrichedList.stream()
                    .filter(p -> "UnknownPokemon".equals(p.getName()))
                    .findFirst().orElse(null);
                assertNotNull(unknownPokemon);
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("electric", null);
        verify(entriesRepository).findEffectiveness("unknown", null);
    }

    @Test
    @DisplayName("Should handle empty Pokemon list")
    void shouldHandleEmptyPokemonList() {
        List<Pokemon> emptyList = Collections.emptyList();

        Mono<List<Pokemon>> result = service.enrichPokemonListWithEffectiveness(emptyList);

        StepVerifier.create(result)
            .assertNext(enrichedList -> {
                assertNotNull(enrichedList);
                assertTrue(enrichedList.isEmpty());
            })
            .verifyComplete();

        verifyNoInteractions(entriesRepository);
    }

    @Test
    @DisplayName("Should calculate complex dual-type effectiveness multipliers")
    void shouldCalculateComplexDualTypeEffectivenessMultipliers() {
        Effectiveness type1 = new Effectiveness("type1",
            Arrays.asList("ghost"),
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("normal"),
            Arrays.asList("water"),
            Arrays.asList("grass")
        );

        Effectiveness type2 = new Effectiveness("type2",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("normal"),
            Arrays.asList("grass"),
            Arrays.asList("electric")
        );

        when(entriesRepository.findEffectiveness("type1", null))
            .thenReturn(Mono.just(type1));
        when(entriesRepository.findEffectiveness("type2", null))
            .thenReturn(Mono.just(type2));

        Mono<Effectiveness> result = service.findEffectiveness("Type1", "Type2");

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("type1 / type2", effectiveness.getTypeName());

                assertTrue(effectiveness.getNoEffect().contains("ghost"),
                    "Ghost: 0.0 * 1.0 = 0.0 (no effect)");
                assertTrue(effectiveness.getSuperEffective().contains("grass"),
                    "Grass: 4.0 * 2.0 = 8.0 ≥ 4.0 (super effective)");
                
                assertNotNull(effectiveness.getDoubleResistant());
                assertNotNull(effectiveness.getNotVeryEffective());
                assertNotNull(effectiveness.getNeutral());
                assertNotNull(effectiveness.getEffective());
            })
            .verifyComplete();

        verify(entriesRepository).findEffectiveness("type1", null);
        verify(entriesRepository).findEffectiveness("type2", null);
    }

    private Pokemon createPikachu() {
        return new Pokemon("Pikachu", 320, "Electric", null, 25, "False", 
                          35, 55, 40, 50, 50, 90, 1, "Kanto");
    }

    private Pokemon createCharmander() {
        return new Pokemon("Charmander", 309, "Fire", null, 4, "False", 
                          39, 52, 43, 60, 50, 65, 1, "Kanto");
    }

    private Pokemon createBulbasaur() {
        return new Pokemon("Bulbasaur", 318, "Grass", "Poison", 1, "False", 
                          45, 49, 49, 65, 65, 45, 1, "Kanto");
    }

    private Pokemon createUnknownTypePokemon() {
        return new Pokemon("UnknownPokemon", 100, "Unknown", null, 999, "False", 
                          50, 50, 50, 50, 50, 50, 1, "Unknown");
    }

    private Effectiveness createFireEffectiveness() {
        return new Effectiveness("fire",
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("fire", "water", "rock", "dragon"),
            Arrays.asList("normal", "electric", "poison", "ground", "flying", "psychic", "ghost", "dark", "steel"),
            Arrays.asList("grass", "ice", "bug", "steel"),
            Collections.emptyList()
        );
    }

    private Effectiveness createWaterEffectiveness() {
        return new Effectiveness("water",
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("water", "grass", "dragon"),
            Arrays.asList("normal", "electric", "ice", "poison", "ground", "flying", "psychic", "bug", "ghost", "dark", "steel", "fairy"),
            Arrays.asList("fire", "rock"),
            Collections.emptyList()
        );
    }

    private Effectiveness createElectricEffectiveness() {
        return new Effectiveness("electric",
            Arrays.asList("ground"),
            Collections.emptyList(),
            Arrays.asList("electric", "grass", "dragon"),
            Arrays.asList("normal", "fire", "ice", "poison", "psychic", "bug", "rock", "ghost", "dark", "steel", "fairy"),
            Arrays.asList("water", "flying"),
            Collections.emptyList()
        );
    }

    private Effectiveness createGrassEffectiveness() {
        return new Effectiveness("grass",
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("fire", "grass", "poison", "flying", "bug", "dragon", "steel"),
            Arrays.asList("normal", "electric", "ice", "fighting", "psychic", "ghost", "dark", "fairy"),
            Arrays.asList("water", "ground", "rock"),
            Collections.emptyList()
        );
    }

    private Effectiveness createPoisonEffectiveness() {
        return new Effectiveness("poison",
            Arrays.asList("steel"),
            Collections.emptyList(),
            Arrays.asList("poison", "ground", "rock", "ghost"),
            Arrays.asList("normal", "fire", "water", "electric", "ice", "fighting", "flying", "bug", "dragon", "dark"),
            Arrays.asList("grass", "fairy"),
            Arrays.asList("psychic")
        );
    }
} 