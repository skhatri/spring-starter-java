package com.github.starter.modules.pokemon.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.starter.modules.pokemon.model.Effectiveness;
import com.github.starter.modules.pokemon.model.Pokemon;
import com.github.starter.modules.pokemon.repository.EntriesRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DefaultEntriesService implements EntriesService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultEntriesService.class);
    
    private final EntriesRepository entriesRepository;
    
    public DefaultEntriesService(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }
    
    private Mono<Pokemon> enrichPokemonWithEffectiveness(Pokemon pokemon) {
        logger.info("Enriching Pokemon: {} with primary type: {}, secondary type: {}", 
                pokemon.getName(), pokemon.getPrimaryType(), pokemon.getSecondaryType());
        
        return findEffectiveness(pokemon.getPrimaryType(), pokemon.getSecondaryType()).map(e -> {
            pokemon.setEffectiveness(e);
            return pokemon;
        });
    }
    
    @Override
    public Mono<Effectiveness> findEffectiveness(String primaryType, String secondaryType) {
        if (secondaryType == null || secondaryType.isEmpty()) {
            return entriesRepository.findEffectiveness(primaryType.toLowerCase(Locale.ROOT), null)
                .doOnNext(e -> logger.info("Found single-type effectiveness for {}: {}", primaryType, e.getTypeName()))
                .onErrorResume(err -> {
                    logger.warn("No effectiveness data found for single type: {}", primaryType);
                    return Mono.just(createEmptyEffectiveness(primaryType));
                });
        } else {
            return calculateDualTypeEffectiveness(primaryType, secondaryType);
        }
    }
    
    private Mono<Effectiveness> calculateDualTypeEffectiveness(String primaryType, String secondaryType) {
        logger.info("Calculating dual-type effectiveness for {} / {}", primaryType, secondaryType);
        
        Mono<Effectiveness> primary = entriesRepository.findEffectiveness(primaryType.toLowerCase(Locale.ROOT), null)
            .onErrorReturn(createEmptyEffectiveness(primaryType));
        
        Mono<Effectiveness> secondary = entriesRepository.findEffectiveness(secondaryType.toLowerCase(Locale.ROOT), null)
            .onErrorReturn(createEmptyEffectiveness(secondaryType));
        
        return Mono.zip(primary, secondary, this::combineDualTypeEffectiveness)
            .doOnNext(e -> logger.info("Combined dual-type effectiveness for {} / {}: {}", 
                    primaryType, secondaryType, e.getTypeName()));
    }
    
    private Effectiveness combineDualTypeEffectiveness(Effectiveness primary, Effectiveness secondary) {
        String combinedTypeName = primary.getTypeName() + " / " + secondary.getTypeName();
        
        Map<String, Double> typeMultipliers = new HashMap<>();
        
        Set<String> allTypes = new HashSet<>();
        
        Set<String> allKnownTypes = Set.of(
            "normal", "fire", "water", "electric", "grass", "ice",
            "fighting", "poison", "ground", "flying", "psychic",
            "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
        );
        
        allTypes.addAll(allKnownTypes);
        allTypes.addAll(primary.getNoEffect());
        allTypes.addAll(primary.getDoubleResistant());
        allTypes.addAll(primary.getNotVeryEffective());
        allTypes.addAll(primary.getNeutral());
        allTypes.addAll(primary.getEffective());
        allTypes.addAll(primary.getSuperEffective());
        allTypes.addAll(secondary.getNoEffect());
        allTypes.addAll(secondary.getDoubleResistant());
        allTypes.addAll(secondary.getNotVeryEffective());
        allTypes.addAll(secondary.getNeutral());
        allTypes.addAll(secondary.getEffective());
        allTypes.addAll(secondary.getSuperEffective());
        
        for (String type : allTypes) {
            double primaryMultiplier = getMultiplierForType(primary, type);
            double secondaryMultiplier = getMultiplierForType(secondary, type);
            double combinedMultiplier = primaryMultiplier * secondaryMultiplier;
            typeMultipliers.put(type, combinedMultiplier);
        }
        
        List<String> noEffect = new ArrayList<>();
        List<String> doubleResistant = new ArrayList<>();
        List<String> notVeryEffective = new ArrayList<>();
        List<String> neutral = new ArrayList<>();
        List<String> effective = new ArrayList<>();
        List<String> superEffective = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : typeMultipliers.entrySet()) {
            double multiplier = entry.getValue();
            String type = entry.getKey();
            
            if (multiplier == 0.0) {
                noEffect.add(type);
            } else if (multiplier == 0.25) {
                doubleResistant.add(type);
            } else if (multiplier == 0.5) {
                notVeryEffective.add(type);
            } else if (multiplier == 1.0) {
                neutral.add(type);
            } else if (multiplier == 2.0) {
                effective.add(type);
            } else if (multiplier >= 4.0) {
                superEffective.add(type);
            }
        }
        
        return new Effectiveness(combinedTypeName, noEffect, doubleResistant, 
                notVeryEffective, neutral, effective, superEffective);
    }
    
    private double getMultiplierForType(Effectiveness effectiveness, String type) {
        if (effectiveness.getNoEffect().contains(type)) return 0.0;
        if (effectiveness.getDoubleResistant().contains(type)) return 0.25;
        if (effectiveness.getNotVeryEffective().contains(type)) return 0.5;
        if (effectiveness.getNeutral().contains(type)) return 1.0;
        if (effectiveness.getEffective().contains(type)) return 2.0;
        if (effectiveness.getSuperEffective().contains(type)) return 4.0;
        return 1.0;
    }
    
    private Effectiveness createEmptyEffectiveness(String typeName) {
        return new Effectiveness(typeName, 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList());
    }
    
    @Override
    public Mono<Pokemon> findByName(String name) {
        logger.info("Finding Pokemon by name: {} (without effectiveness)", name);
        return entriesRepository.findByName(name)
                .doOnNext(p -> logger.info("Found Pokemon: {}", p.getName()))
                .doOnError(e -> logger.error("Error finding Pokemon {}: {}", name, e.getMessage()));
    }
    
    @Override
    public Mono<Pokemon> findByNameWithEffectiveness(String name) {
        logger.info("Finding Pokemon by name: {} (with effectiveness)", name);
        return entriesRepository.findByName(name)
                .doOnNext(p -> logger.info("Found Pokemon: {}", p.getName()))
                .doOnError(e -> logger.error("Error finding Pokemon {}: {}", name, e.getMessage()))
                .flatMap(this::enrichPokemonWithEffectiveness)
                .doOnNext(p -> {
                    if (p.getEffectiveness() == null) {
                        logger.warn("Final Pokemon has null effectiveness: {}", p.getName());
                    } else {
                        logger.info("Final Pokemon has effectiveness: {}", p.getEffectiveness().getTypeName());
                    }
                });
    }
    
    @Override
    public Mono<Effectiveness> getEffectivenessForPokemon(Pokemon pokemon) {
        logger.info("Getting effectiveness for Pokemon: {} ({} / {})", 
                pokemon.getName(), pokemon.getPrimaryType(), pokemon.getSecondaryType());
        
        if (pokemon.getEffectiveness() != null) {
            logger.info("Pokemon already has effectiveness data: {}", pokemon.getName());
            return Mono.just(pokemon.getEffectiveness());
        }
        
        return findEffectiveness(pokemon.getPrimaryType(), pokemon.getSecondaryType())
                .doOnNext(e -> {
                    if (e != null) {
                        logger.info("Calculated effectiveness for Pokemon {}: {}", 
                                pokemon.getName(), e.getTypeName());
                    } else {
                        logger.warn("Failed to calculate effectiveness for Pokemon: {}", 
                                pokemon.getName());
                    }
                });
    }
    
    @Override
    public Mono<List<Pokemon>> enrichPokemonListWithEffectiveness(List<Pokemon> pokemonList) {
        logger.info("Enriching {} Pokemon with effectiveness data", pokemonList.size());
        
        return Flux.fromIterable(pokemonList)
            .flatMap(pokemon -> getEffectivenessForPokemon(pokemon)
                .map(effectiveness -> {
                    pokemon.setEffectiveness(effectiveness);
                    return pokemon;
                })
                .onErrorReturn(pokemon))
            .collectList()
            .doOnNext(enrichedList -> logger.info("Successfully enriched {} Pokemon with effectiveness", 
                    enrichedList.size()));
    }
} 