package com.github.starter;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.security.SecureRandom;
import java.util.List;

@DisabledIf("com.github.starter.Conditions#ignoreMock")
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class WireMockTests {
    private static WireMockContainer wiremockServer;
    private static int port;

    @BeforeAll
    public static void beforeTest() {
        wiremockServer = new WireMockContainer("wiremock/wiremock:3.9.1").withMappingFromResource("health", "mock/status.json").withMappingFromResource("pokemon", "mock/pokemon.json").withExposedPorts(8080).withAccessToHost(true);
        port = 40000 + new SecureRandom().nextInt(100);
        wiremockServer.setPortBindings(List.of(String.format("%d:8080", port)));
        wiremockServer.start();
    }

    @Karate.Test
    Karate statusTest() {
        return runFeature("classpath:karate/status.feature");
    }

    @Karate.Test
    Karate pokemonTest() {
        return runFeature("classpath:karate/pokemon/pokemon.feature");
    }

    private Karate runFeature(String file) {
        return Karate.run(file).systemProperty("server.port", String.valueOf(port));
    }

    @AfterAll
    public static void afterTest() {
        wiremockServer.close();
    }
}

