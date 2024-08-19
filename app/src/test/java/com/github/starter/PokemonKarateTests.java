package com.github.starter;

import com.intuit.karate.junit5.Karate;

class PokemonKarateTests {

    @Karate.Test
    Karate dummyTest() {
        return Karate.run("classpath:karate/pokemon/placeholder.feature");
    }

    @Karate.Test
    Karate statusTest() {
        return Karate.run("classpath:karate/pokemon/status.feature");
    }

    @Karate.Test
    Karate pokemonTest() {
        return Karate.run("classpath:karate/pokemon/pokemon.feature");
    }
}

