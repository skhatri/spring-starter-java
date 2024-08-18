package com.github.starter;

import com.intuit.karate.junit5.Karate;

public class GraphQLPokemonTests {

    @Karate.Test
    public Karate testPokemonList() {
        return Karate.run("classpath:karate/pokemon/graphql.feature");
    }
}
