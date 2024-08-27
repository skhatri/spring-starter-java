package com.github.starter;

import com.intuit.karate.junit5.Karate;

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