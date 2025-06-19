package com.github.starter.modules.pokemon.endpoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.starter.modules.pagination.adapter.graphql.GraphQLPaginationAdapter;
import com.github.starter.modules.pagination.adapter.rest.PaginatedResponse;
import com.github.starter.modules.pagination.adapter.rest.RestPaginationAdapter;
import com.github.starter.modules.pagination.model.Page;
import com.github.starter.modules.pagination.model.PaginationRequest;
import com.github.starter.modules.pagination.service.PaginationService;
import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.model.PokemonPage;
import com.github.starter.modules.pokemon.service.EntriesService;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("pokemon")
public class EntriesEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(EntriesEndpoint.class);
    
    private final EntriesService entriesService;
    private final PaginationService<Pokemon> paginationService;
    private final RestPaginationAdapter restPaginationAdapter;
    private final GraphQLPaginationAdapter graphqlPaginationAdapter;
    private final Tracer tracer;

    public EntriesEndpoint(
        EntriesService entriesService,
        PaginationService<Pokemon> paginationService,
        RestPaginationAdapter restPaginationAdapter,
        GraphQLPaginationAdapter graphqlPaginationAdapter,
        Tracer tracer
    ) {
        this.entriesService = entriesService;
        this.paginationService = paginationService;
        this.restPaginationAdapter = restPaginationAdapter;
        this.graphqlPaginationAdapter = graphqlPaginationAdapter;
        this.tracer = tracer;
    }

    @GetMapping("/list")
    @WithSpan("pokemon.list.rest")
    public Mono<PaginatedResponse<Pokemon>> getPokemonList(
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String nextToken,
        @RequestParam(required = false) String previousToken,
        @RequestParam(required = false) String sortField,
        @RequestParam(required = false) String sortDirection,
        @RequestParam(required = false, defaultValue = "false") Boolean effectiveness
    ) {
        Span span = tracer.spanBuilder("pokemon.list.rest")
                .setAttribute("pokemon.limit", limit != null ? limit : 0)
                .setAttribute("pokemon.sort_field", sortField != null ? sortField : "none")
                .setAttribute("pokemon.effectiveness", effectiveness)
                .startSpan();
        
        logger.info("REST Pokemon list request - limit: {}, nextToken: {}, sortField: {}, effectiveness: {}", 
                   limit, nextToken, sortField, effectiveness);
        
        PaginationRequest request = restPaginationAdapter.toPaginationRequest(
            limit, nextToken, previousToken, sortField, sortDirection
        );
        
        return paginationService.findPage(request)
            .flatMap(page -> effectiveness ? 
                enrichPokemonPageWithEffectiveness(page) : 
                Mono.just(page))
            .map(page -> restPaginationAdapter.toPaginatedResponse(page, request))
            .doOnNext(response -> {
                span.setAttribute("pokemon.result_count", response.data().size());
                span.setAttribute("pokemon.has_next", response.pagination().hasNext());
            })
            .doOnError(error -> {
                span.recordException(error);
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
            })
            .doFinally(signalType -> span.end());
    }
    
    @QueryMapping(name = "getPokemonList")
    @WithSpan("pokemon.list.graphql")
    public Mono<PokemonPage> getPokemonList(
        @Argument(name = "limit") Integer limit,
        @Argument(name = "cursor") String cursor,
        @Argument(name = "sortField") String sortField,
        @Argument(name = "sortDirection") String sortDirection,
        DataFetchingEnvironment environment
    ) {
        Set<String> requestedFields = getRequestedFieldNames(environment);
        
        Span span = tracer.spanBuilder("pokemon.list.graphql")
                .setAttribute("pokemon.limit", limit != null ? limit : 0)
                .setAttribute("pokemon.sort_field", sortField != null ? sortField : "none")
                .setAttribute("pokemon.requested_fields", String.join(",", requestedFields))
                .startSpan();
        
        logger.info("GraphQL getPokemonList - limit: {}, cursor: {}, sortField: {}, requested fields: {}", 
                   limit, cursor, sortField, requestedFields);
        
        PaginationRequest request = graphqlPaginationAdapter.toPaginationRequest(
            limit, cursor, null, sortField, sortDirection
        );
        
        return paginationService.findPage(request)
            .map(PokemonPage::fromPage)
            .doOnNext(pokemonPage -> {
                span.setAttribute("pokemon.result_count", pokemonPage.content().size());
                span.setAttribute("pokemon.has_next", pokemonPage.hasNext());
            })
            .doOnError(error -> {
                span.recordException(error);
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
            })
            .doFinally(signalType -> span.end());
    }
    
    @QueryMapping(name = "effectiveness")
    @GetMapping("/effectiveness/{typeName}")
    @WithSpan("pokemon.effectiveness")
    public Mono<Effectiveness> getEffectiveness(@PathVariable @Argument("typeName") String typeName) {
        List<String> types = List.of(typeName.split(" ", 2));
        String primaryType = types.get(0);
        String secondaryType = types.size() > 1 ? types.get(1) : null;
        
        Span span = tracer.spanBuilder("pokemon.effectiveness")
                .setAttribute("pokemon.type_name", typeName)
                .setAttribute("pokemon.primary_type", primaryType)
                .setAttribute("pokemon.secondary_type", secondaryType != null ? secondaryType : "none")
                .startSpan();
        
        logger.info("GraphQL effectiveness query for typeName: '{}', primaryType: '{}', secondaryType: '{}'", 
                typeName, primaryType, secondaryType);
        
        return entriesService.findEffectiveness(primaryType, secondaryType)
                .doOnNext(e -> {
                    logger.info("Found effectiveness for type: {} with {} no-effect types, {} double-resistant types, {} not-very-effective types, {} neutral types, {} effective types, {} super-effective types", 
                            e.getTypeName(), 
                            e.getNoEffect().size(), 
                            e.getDoubleResistant().size(),
                            e.getNotVeryEffective().size(),
                            e.getNeutral().size(),
                            e.getEffective().size(),
                            e.getSuperEffective().size());
                    
                    span.setAttribute("pokemon.effectiveness.no_effect_count", e.getNoEffect().size());
                    span.setAttribute("pokemon.effectiveness.super_effective_count", e.getSuperEffective().size());
                })
                .doOnError(err -> {
                    logger.error("Error fetching effectiveness for type {}: {}", typeName, err.getMessage());
                    span.recordException(err);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, err.getMessage());
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    logger.warn("No effectiveness data found for type: {}", typeName);
                    span.setAttribute("pokemon.effectiveness.found", false);
                    return new Effectiveness(typeName, 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList());
                }))
                .doFinally(signalType -> span.end());
    }
    
    @GetMapping("/{name}")
    @WithSpan("pokemon.get_by_name.rest")
    public Mono<Pokemon> getPokemonByName(@PathVariable @Argument("name") String name) {
        Span span = tracer.spanBuilder("pokemon.get_by_name.rest")
                .setAttribute("pokemon.name", name)
                .startSpan();
        
        logger.info("Getting Pokemon by name: {} (REST endpoint - with effectiveness)", name);
        
        return entriesService.findByNameWithEffectiveness(name)
                .doOnNext(pokemon -> span.setAttribute("pokemon.found", true))
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signalType -> span.end());
    }
    
    @SchemaMapping(typeName = "Query", field = "pokemon")
    @WithSpan("pokemon.get_by_name.graphql")
    public Mono<Pokemon> getPokemonByName(@Argument("name") String name, DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFieldNames(environment);
        
        Span span = tracer.spanBuilder("pokemon.get_by_name.graphql")
                .setAttribute("pokemon.name", name)
                .setAttribute("pokemon.requested_fields", String.join(",", requestedFields))
                .startSpan();
        
        logger.info("GraphQL pokemon query for {} requested fields: {}", name, requestedFields);
        
        return entriesService.findByName(name)
                .doOnNext(pokemon -> span.setAttribute("pokemon.found", true))
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signalType -> span.end());
    }
    
    @SchemaMapping(typeName = "Pokemon", field = "effectiveness")
    @WithSpan("pokemon.effectiveness.on_demand")
    public Mono<Effectiveness> getEffectivenessForPokemon(Pokemon pokemon) {
        Span span = tracer.spanBuilder("pokemon.effectiveness.on_demand")
                .setAttribute("pokemon.name", pokemon.getName())
                .startSpan();
        
        logger.info("Fetching effectiveness for Pokemon {} on-demand", pokemon.getName());
        
        return entriesService.getEffectivenessForPokemon(pokemon)
                .doOnNext(effectiveness -> span.setAttribute("pokemon.effectiveness.loaded", true))
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signalType -> span.end());
    }
    
    @WithSpan("pokemon.enrich_with_effectiveness")
    private Mono<Page<Pokemon>> enrichPokemonPageWithEffectiveness(Page<Pokemon> page) {
        Span span = tracer.spanBuilder("pokemon.enrich_with_effectiveness")
                .setAttribute("pokemon.count", page.content().size())
                .startSpan();
        
        logger.info("Enriching {} Pokemon with effectiveness data", page.content().size());
        
        return entriesService.enrichPokemonListWithEffectiveness(page.content())
            .map(enrichedContent -> new Page<>(
                enrichedContent,
                page.nextCursor(),
                page.previousCursor(),
                page.hasNext(),
                page.hasPrevious(),
                page.totalCount(),
                page.pageSize()
            ))
            .doOnNext(enrichedPage -> span.setAttribute("pokemon.enriched_count", enrichedPage.content().size()))
            .doOnError(error -> {
                span.recordException(error);
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
            })
            .doFinally(signalType -> span.end());
    }
    
    private Set<String> getRequestedFieldNames(DataFetchingEnvironment environment) {
        Set<String> fieldNames = new HashSet<>();
        
        List<SelectedField> selectedFields = environment.getSelectionSet().getFields();
        
        for (SelectedField field : selectedFields) {
            fieldNames.add(field.getName());
        }
        
        return fieldNames;
    }
}
