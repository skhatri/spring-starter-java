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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("pokemon")
public class EntriesEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(EntriesEndpoint.class);
    
    private final EntriesService entriesService;
    private final PaginationService<Pokemon> paginationService;
    private final RestPaginationAdapter restPaginationAdapter;
    private final GraphQLPaginationAdapter graphqlPaginationAdapter;

    public EntriesEndpoint(
        EntriesService entriesService,
        PaginationService<Pokemon> paginationService,
        RestPaginationAdapter restPaginationAdapter,
        GraphQLPaginationAdapter graphqlPaginationAdapter
    ) {
        this.entriesService = entriesService;
        this.paginationService = paginationService;
        this.restPaginationAdapter = restPaginationAdapter;
        this.graphqlPaginationAdapter = graphqlPaginationAdapter;
    }

    @GetMapping("/list")
    public Mono<PaginatedResponse<Pokemon>> getPokemonList(
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String nextToken,
        @RequestParam(required = false) String previousToken,
        @RequestParam(required = false) String sortField,
        @RequestParam(required = false) String sortDirection,
        @RequestParam(required = false, defaultValue = "false") Boolean effectiveness
    ) {
        logger.info("REST Pokemon list request - limit: {}, nextToken: {}, sortField: {}, effectiveness: {}", 
                   limit, nextToken, sortField, effectiveness);
        
        PaginationRequest request = restPaginationAdapter.toPaginationRequest(
            limit, nextToken, previousToken, sortField, sortDirection
        );
        
        return paginationService.findPage(request)
            .flatMap(page -> effectiveness ? 
                enrichPokemonPageWithEffectiveness(page) : 
                Mono.just(page))
            .map(page -> restPaginationAdapter.toPaginatedResponse(page, request));
    }
    
    @QueryMapping(name = "getPokemonList")
    public Mono<PokemonPage> getPokemonList(
        @Argument(name = "limit") Integer limit,
        @Argument(name = "cursor") String cursor,
        @Argument(name = "sortField") String sortField,
        @Argument(name = "sortDirection") String sortDirection,
        DataFetchingEnvironment environment
    ) {
        Set<String> requestedFields = getRequestedFieldNames(environment);
        logger.info("GraphQL getPokemonList - limit: {}, cursor: {}, sortField: {}, requested fields: {}", 
                   limit, cursor, sortField, requestedFields);
        
        PaginationRequest request = graphqlPaginationAdapter.toPaginationRequest(
            limit, cursor, null, sortField, sortDirection
        );
        
        return paginationService.findPage(request)
            .map(PokemonPage::fromPage);
    }
    
    @QueryMapping(name = "effectiveness")
    @GetMapping("/effectiveness/{typeName}")
    public Mono<Effectiveness> getEffectiveness(@PathVariable @Argument("typeName") String typeName) {
        String[] types = typeName.split(" ");
        String primaryType = types[0];
        String secondaryType = types.length > 1 ? types[1] : null;
        logger.info("GraphQL effectiveness query for typeName: '{}', primaryType: '{}', secondaryType: '{}'", 
                typeName, primaryType, secondaryType);
        return entriesService.findEffectiveness(primaryType, secondaryType)
                .doOnNext(e -> logger.info("Found effectiveness for type: {} with {} no-effect types, {} double-resistant types, {} not-very-effective types, {} neutral types, {} effective types, {} super-effective types", 
                        e.getTypeName(), 
                        e.getNoEffect().size(), 
                        e.getDoubleResistant().size(),
                        e.getNotVeryEffective().size(),
                        e.getNeutral().size(),
                        e.getEffective().size(),
                        e.getSuperEffective().size()))
                .doOnError(err -> logger.error("Error fetching effectiveness for type {}: {}", typeName, err.getMessage()))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    logger.warn("No effectiveness data found for type: {}", typeName);
                    return new Effectiveness(typeName, 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList(), 
                            Collections.emptyList());
                }));
    }
    
    @GetMapping("/{name}")
    public Mono<Pokemon> getPokemonByName(@PathVariable @Argument("name") String name) {
        logger.info("Getting Pokemon by name: {} (REST endpoint - with effectiveness)", name);
        return entriesService.findByNameWithEffectiveness(name);
    }
    
    @SchemaMapping(typeName = "Query", field = "pokemon")
    public Mono<Pokemon> getPokemonByName(@Argument("name") String name, DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFieldNames(environment);
        logger.info("GraphQL pokemon query for {} requested fields: {}", name, requestedFields);
        return entriesService.findByName(name);
    }
    
    @SchemaMapping(typeName = "Pokemon", field = "effectiveness")
    public Mono<Effectiveness> getEffectivenessForPokemon(Pokemon pokemon) {
        logger.info("Fetching effectiveness for Pokemon {} on-demand", pokemon.getName());
        return entriesService.getEffectivenessForPokemon(pokemon);
    }
    
    private Mono<Page<Pokemon>> enrichPokemonPageWithEffectiveness(Page<Pokemon> page) {
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
            ));
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
