package com.github.starter;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.boot.test.context.SpringBootTest;

@DisabledIf("com.github.starter.Conditions#ignoreBoot")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class KarateTests {

    @Karate.Test
    Karate dummyTest() {
        return Karate.run("classpath:karate/placeholder.feature");
    }

    @Karate.Test
    Karate statusTest() {
        return Karate.run("classpath:karate/status.feature");
    }

    @Karate.Test
    Karate pokemonTest() {
        return Karate.run("classpath:karate/pokemon/pokemon.feature");
    }
}

