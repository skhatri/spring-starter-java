package com.github.starter.modules.pokemon.endpoint;

import com.github.starter.modules.pagination.adapter.graphql.GraphQLPaginationAdapter;
import com.github.starter.modules.pagination.adapter.rest.PaginatedResponse;
import com.github.starter.modules.pagination.adapter.rest.RestPaginationAdapter;
import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.model.SortDirection;
import com.github.starter.modules.pagination.service.PaginationService;
import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.model.PokemonPage;
import com.github.starter.modules.pokemon.service.EntriesService;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntriesEndpoint Tests")
class EntriesEndpointTest {

    @Mock
    private EntriesService entriesService;

    @Mock
    private PaginationService<Pokemon> paginationService;

    @Mock
    private RestPaginationAdapter restPaginationAdapter;

    @Mock
    private GraphQLPaginationAdapter graphqlPaginationAdapter;

    @Mock
    private DataFetchingEnvironment dataFetchingEnvironment;

    @Mock
    private DataFetchingFieldSelectionSet selectionSet;

    @Mock
    private SelectedField selectedField;

    @Mock
    private io.opentelemetry.api.trace.Tracer tracer;

    @Mock
    private io.opentelemetry.api.trace.SpanBuilder spanBuilder;

    @Mock
    private io.opentelemetry.api.trace.Span span;

    private EntriesEndpoint endpoint;

    @BeforeEach
    void setUp() {
        when(tracer.spanBuilder(any(String.class))).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(any(String.class), any(String.class))).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(any(String.class), any(Boolean.class))).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(any(String.class), any(Integer.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.setAttribute(any(String.class), any(String.class))).thenReturn(span);
        when(span.setAttribute(any(String.class), any(Boolean.class))).thenReturn(span);
        when(span.setAttribute(any(String.class), any(Integer.class))).thenReturn(span);
        
        endpoint = new EntriesEndpoint(
            entriesService,
            paginationService,
            restPaginationAdapter,
            graphqlPaginationAdapter,
            tracer
        );
    }

    @Test
    @DisplayName("Should get Pokemon list via REST without effectiveness")
    void shouldGetPokemonListViaRestWithoutEffectiveness() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();
        PaginatedResponse<Pokemon> response = createPaginatedResponse();

        when(restPaginationAdapter.toPaginationRequest(10, "token", null, "name", "ASC"))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));
        when(restPaginationAdapter.toPaginatedResponse(page, request))
            .thenReturn(response);

        Mono<PaginatedResponse<Pokemon>> result = endpoint.getPokemonList(
            10, "token", null, "name", "ASC", false
        );

        StepVerifier.create(result)
            .assertNext(paginatedResponse -> {
                assertNotNull(paginatedResponse);
                assertEquals(response, paginatedResponse);
            })
            .verifyComplete();

        verify(restPaginationAdapter).toPaginationRequest(10, "token", null, "name", "ASC");
        verify(paginationService).findPage(request);
        verify(restPaginationAdapter).toPaginatedResponse(page, request);
        verifyNoInteractions(entriesService);
    }

    @Test
    @DisplayName("Should get Pokemon list via REST with effectiveness")
    void shouldGetPokemonListViaRestWithEffectiveness() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();
        Page<Pokemon> enrichedPage = createEnrichedPokemonPage();
        PaginatedResponse<Pokemon> response = createPaginatedResponse();

        when(restPaginationAdapter.toPaginationRequest(5, null, "prev", "attack", "DESC"))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));
        when(entriesService.enrichPokemonListWithEffectiveness(page.content()))
            .thenReturn(Mono.just(enrichedPage.content()));
        when(restPaginationAdapter.toPaginatedResponse(any(Page.class), eq(request)))
            .thenReturn(response);

        Mono<PaginatedResponse<Pokemon>> result = endpoint.getPokemonList(
            5, null, "prev", "attack", "DESC", true
        );

        StepVerifier.create(result)
            .assertNext(paginatedResponse -> {
                assertNotNull(paginatedResponse);
                assertEquals(response, paginatedResponse);
            })
            .verifyComplete();

        verify(restPaginationAdapter).toPaginationRequest(5, null, "prev", "attack", "DESC");
        verify(paginationService).findPage(request);
        verify(entriesService).enrichPokemonListWithEffectiveness(page.content());
        verify(restPaginationAdapter).toPaginatedResponse(any(Page.class), eq(request));
    }

    @Test
    @DisplayName("Should get Pokemon list via GraphQL")
    void shouldGetPokemonListViaGraphQL() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();
        List<SelectedField> fields = Arrays.asList(selectedField);

        when(dataFetchingEnvironment.getSelectionSet()).thenReturn(selectionSet);
        when(selectionSet.getFields()).thenReturn(fields);
        when(selectedField.getName()).thenReturn("name");
        when(graphqlPaginationAdapter.toPaginationRequest(20, "cursor123", null, "id", "ASC"))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));

        Mono<PokemonPage> result = endpoint.getPokemonList(
            20, "cursor123", "id", "ASC", dataFetchingEnvironment
        );

        StepVerifier.create(result)
            .assertNext(pokemonPage -> {
                assertNotNull(pokemonPage);
                assertEquals(page.content().size(), pokemonPage.content().size());
            })
            .verifyComplete();

        verify(graphqlPaginationAdapter).toPaginationRequest(20, "cursor123", null, "id", "ASC");
        verify(paginationService).findPage(request);
        verify(dataFetchingEnvironment).getSelectionSet();
    }

    @Test
    @DisplayName("Should get effectiveness for single type")
    void shouldGetEffectivenessForSingleType() {
        Effectiveness effectiveness = createFireEffectiveness();

        when(entriesService.findEffectiveness("fire", null))
            .thenReturn(Mono.just(effectiveness));

        Mono<Effectiveness> result = endpoint.getEffectiveness("fire");

        StepVerifier.create(result)
            .assertNext(eff -> {
                assertNotNull(eff);
                assertEquals("fire", eff.getTypeName());
            })
            .verifyComplete();

        verify(entriesService).findEffectiveness("fire", null);
    }

    @Test
    @DisplayName("Should get effectiveness for dual type")
    void shouldGetEffectivenessForDualType() {
        Effectiveness effectiveness = createDualTypeEffectiveness();

        when(entriesService.findEffectiveness("grass", "poison"))
            .thenReturn(Mono.just(effectiveness));

        Mono<Effectiveness> result = endpoint.getEffectiveness("grass poison");

        StepVerifier.create(result)
            .assertNext(eff -> {
                assertNotNull(eff);
                assertEquals("grass poison", eff.getTypeName());
            })
            .verifyComplete();

        verify(entriesService).findEffectiveness("grass", "poison");
    }

    @Test
    @DisplayName("Should handle effectiveness not found")
    void shouldHandleEffectivenessNotFound() {
        when(entriesService.findEffectiveness("unknown", null))
            .thenReturn(Mono.empty());

        Mono<Effectiveness> result = endpoint.getEffectiveness("unknown");

        StepVerifier.create(result)
            .assertNext(eff -> {
                assertNotNull(eff);
                assertEquals("unknown", eff.getTypeName());
                assertEquals(0, eff.getNoEffect().size());
                assertEquals(0, eff.getEffective().size());
            })
            .verifyComplete();

        verify(entriesService).findEffectiveness("unknown", null);
    }

    @Test
    @DisplayName("Should handle effectiveness error")
    void shouldHandleEffectivenessError() {
        when(entriesService.findEffectiveness("error", null))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Effectiveness> result = endpoint.getEffectiveness("error");

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(entriesService).findEffectiveness("error", null);
    }

    @Test
    @DisplayName("Should get Pokemon by name via REST")
    void shouldGetPokemonByNameViaRest() {
        Pokemon pikachu = createPikachuWithEffectiveness();

        when(entriesService.findByNameWithEffectiveness("Pikachu"))
            .thenReturn(Mono.just(pikachu));

        Mono<Pokemon> result = endpoint.getPokemonByName("Pikachu");

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Pikachu", pokemon.getName());
                assertNotNull(pokemon.getEffectiveness());
            })
            .verifyComplete();

        verify(entriesService).findByNameWithEffectiveness("Pikachu");
    }

    @Test
    @DisplayName("Should get Pokemon by name via GraphQL")
    void shouldGetPokemonByNameViaGraphQL() {
        Pokemon bulbasaur = createBulbasaur();
        List<SelectedField> fields = Arrays.asList(selectedField);

        when(dataFetchingEnvironment.getSelectionSet()).thenReturn(selectionSet);
        when(selectionSet.getFields()).thenReturn(fields);
        when(selectedField.getName()).thenReturn("name");
        when(entriesService.findByName("Bulbasaur"))
            .thenReturn(Mono.just(bulbasaur));

        Mono<Pokemon> result = endpoint.getPokemonByName("Bulbasaur", dataFetchingEnvironment);

        StepVerifier.create(result)
            .assertNext(pokemon -> {
                assertNotNull(pokemon);
                assertEquals("Bulbasaur", pokemon.getName());
            })
            .verifyComplete();

        verify(entriesService).findByName("Bulbasaur");
        verify(dataFetchingEnvironment).getSelectionSet();
    }

    @Test
    @DisplayName("Should get effectiveness for Pokemon on-demand")
    void shouldGetEffectivenessForPokemonOnDemand() {
        Pokemon charmander = createCharmander();
        Effectiveness fireEffectiveness = createFireEffectiveness();

        when(entriesService.getEffectivenessForPokemon(charmander))
            .thenReturn(Mono.just(fireEffectiveness));

        Mono<Effectiveness> result = endpoint.getEffectivenessForPokemon(charmander);

        StepVerifier.create(result)
            .assertNext(effectiveness -> {
                assertNotNull(effectiveness);
                assertEquals("fire", effectiveness.getTypeName());
            })
            .verifyComplete();

        verify(entriesService).getEffectivenessForPokemon(charmander);
    }

    @Test
    @DisplayName("Should handle REST pagination with null parameters")
    void shouldHandleRestPaginationWithNullParameters() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();
        PaginatedResponse<Pokemon> response = createPaginatedResponse();

        when(restPaginationAdapter.toPaginationRequest(null, null, null, null, null))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));
        when(restPaginationAdapter.toPaginatedResponse(page, request))
            .thenReturn(response);

        Mono<PaginatedResponse<Pokemon>> result = endpoint.getPokemonList(
            null, null, null, null, null, false
        );

        StepVerifier.create(result)
            .assertNext(paginatedResponse -> {
                assertNotNull(paginatedResponse);
            })
            .verifyComplete();

        verify(restPaginationAdapter).toPaginationRequest(null, null, null, null, null);
        verify(paginationService).findPage(request);
    }

    @Test
    @DisplayName("Should handle GraphQL pagination with null parameters")
    void shouldHandleGraphQLPaginationWithNullParameters() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();
        List<SelectedField> fields = Collections.emptyList();

        when(dataFetchingEnvironment.getSelectionSet()).thenReturn(selectionSet);
        when(selectionSet.getFields()).thenReturn(fields);
        when(graphqlPaginationAdapter.toPaginationRequest(null, null, null, null, null))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));

        Mono<PokemonPage> result = endpoint.getPokemonList(
            null, null, null, null, dataFetchingEnvironment
        );

        StepVerifier.create(result)
            .assertNext(pokemonPage -> {
                assertNotNull(pokemonPage);
            })
            .verifyComplete();

        verify(graphqlPaginationAdapter).toPaginationRequest(null, null, null, null, null);
        verify(paginationService).findPage(request);
    }

    @Test
    @DisplayName("Should handle Pokemon not found error")
    void shouldHandlePokemonNotFoundError() {
        when(entriesService.findByNameWithEffectiveness("NonExistent"))
            .thenReturn(Mono.error(new RuntimeException("Pokemon not found")));

        Mono<Pokemon> result = endpoint.getPokemonByName("NonExistent");

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(entriesService).findByNameWithEffectiveness("NonExistent");
    }

    @Test
    @DisplayName("Should handle pagination service error")
    void shouldHandlePaginationServiceError() {
        PaginationRequest request = createPaginationRequest();

        when(restPaginationAdapter.toPaginationRequest(10, null, null, "name", "ASC"))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        Mono<PaginatedResponse<Pokemon>> result = endpoint.getPokemonList(
            10, null, null, "name", "ASC", false
        );

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(paginationService).findPage(request);
    }

    @Test
    @DisplayName("Should handle enrichment service error gracefully")
    void shouldHandleEnrichmentServiceErrorGracefully() {
        PaginationRequest request = createPaginationRequest();
        Page<Pokemon> page = createPokemonPage();

        when(restPaginationAdapter.toPaginationRequest(5, null, null, "name", "ASC"))
            .thenReturn(request);
        when(paginationService.findPage(request))
            .thenReturn(Mono.just(page));
        when(entriesService.enrichPokemonListWithEffectiveness(anyList()))
            .thenReturn(Mono.error(new RuntimeException("Enrichment failed")));

        Mono<PaginatedResponse<Pokemon>> result = endpoint.getPokemonList(
            5, null, null, "name", "ASC", true
        );

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(entriesService).enrichPokemonListWithEffectiveness(anyList());
    }

    private PaginationRequest createPaginationRequest() {
        return new PaginationRequest(Optional.empty(), 10, SortDirection.ASC, "name");
    }

    private Page<Pokemon> createPokemonPage() {
        List<Pokemon> content = Arrays.asList(
            createPikachu(),
            createCharmander(),
            createBulbasaur()
        );
        return new Page<>(content, Optional.of("next123"), Optional.of("prev123"), true, true, 100L, 10);
    }

    private Page<Pokemon> createEnrichedPokemonPage() {
        List<Pokemon> content = Arrays.asList(
            createPikachuWithEffectiveness(),
            createCharmanderWithEffectiveness(),
            createBulbasaurWithEffectiveness()
        );
        return new Page<>(content, Optional.of("next123"), Optional.of("prev123"), true, true, 100L, 10);
    }

    private PaginatedResponse<Pokemon> createPaginatedResponse() {
        return PaginatedResponse.create(
            Arrays.asList(createPikachu(), createCharmander()),
            new PaginatedResponse.PaginationMetadata(10, 100L, true, true, Optional.of("next123"), Optional.of("prev123"))
        );
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

    private Pokemon createPikachuWithEffectiveness() {
        Pokemon pikachu = createPikachu();
        pikachu.setEffectiveness(createElectricEffectiveness());
        return pikachu;
    }

    private Pokemon createCharmanderWithEffectiveness() {
        Pokemon charmander = createCharmander();
        charmander.setEffectiveness(createFireEffectiveness());
        return charmander;
    }

    private Pokemon createBulbasaurWithEffectiveness() {
        Pokemon bulbasaur = createBulbasaur();
        bulbasaur.setEffectiveness(createDualTypeEffectiveness());
        return bulbasaur;
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

    private Effectiveness createDualTypeEffectiveness() {
        return new Effectiveness("grass poison",
            Collections.emptyList(),
            Collections.emptyList(),
            Arrays.asList("fire", "grass", "poison", "flying", "bug", "dragon", "steel"),
            Arrays.asList("normal", "electric", "ice", "fighting", "psychic", "ghost", "dark", "fairy"),
            Arrays.asList("water", "ground", "rock"),
            Arrays.asList("psychic")
        );
    }
}
