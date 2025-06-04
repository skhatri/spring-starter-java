package com.github.starter;

import org.junit.jupiter.api.Tag;

import com.intuit.karate.junit5.Karate;

@Tag("integration")
public class GraphQLPokemonTests {
    
    @Karate.Test
    public Karate testPokemonList() {
        return Karate.run("classpath:karate/pokemon/graphql.feature");
    }
}
