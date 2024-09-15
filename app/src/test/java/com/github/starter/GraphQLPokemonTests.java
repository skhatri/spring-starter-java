package com.github.starter;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class GraphQLPokemonTests {

    @Karate.Test
    public Karate testPokemonList() {
        return Karate.run("classpath:karate/pokemon/graphql.feature");
    }
}
