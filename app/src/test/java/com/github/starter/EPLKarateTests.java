package com.github.starter;

import com.intuit.karate.junit5.Karate;

public class EPLKarateTests {

    @Karate.Test
    public Karate testPokemonList() {
        return Karate.run("classpath:karate/epl/epl.feature");
    }
}
