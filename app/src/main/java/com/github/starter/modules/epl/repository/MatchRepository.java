package com.github.starter.modules.epl.repository;

import com.github.starter.modules.epl.model.EplMatch;
import com.github.starter.modules.epl.model.EplStanding;
import reactor.core.publisher.Flux;

public interface MatchRepository {
    Flux<EplMatch> biggestMargin(int season);

    Flux<EplMatch> mostGoalsScored(int season);

    Flux<EplStanding> seasonPerformance(String team, int season);

    Flux<EplStanding> seasonTable(int season);

    Flux<EplStanding> winner(int season);

    Flux<String> allTeams(int season);
}
