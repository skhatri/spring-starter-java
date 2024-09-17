package com.github.starter;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.boot.test.context.SpringBootTest;

@DisabledIf("com.github.starter.Conditions#ignoreBoot")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class GraphQLEPLTests {

    @Karate.Test
    public Karate testPremierLeagueStandings() {
        return Karate.run("classpath:karate/epl/standings.feature");
    }

    @Karate.Test
    public Karate testGoals() {
        return Karate.run("classpath:karate/epl/goals.feature");
    }

    @Karate.Test
    public Karate testWinnersForPremierLeagueSeasons() {
        return Karate.run("classpath:karate/epl/winners.feature");
    }
}
