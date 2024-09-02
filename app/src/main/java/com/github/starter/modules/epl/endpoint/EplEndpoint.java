package com.github.starter.modules.epl.endpoint;

import com.github.starter.modules.epl.model.EplMatch;
import com.github.starter.modules.epl.model.EplStanding;
import com.github.starter.modules.epl.repository.MatchRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("epl")
public class EplEndpoint {
    private final MatchRepository matchRepository;

    public EplEndpoint(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @QueryMapping
    @GetMapping("/biggest-margin")
    public Flux<EplMatch> biggestMargin(@Argument(name = "season")
                                        @RequestParam(name = "season", defaultValue = "-1") int season) {
        return matchRepository.biggestMargin(season);
    }

    @QueryMapping
    @GetMapping("/most-goals")
    public Flux<EplMatch> mostGoals(@Argument(name = "season")
                                    @RequestParam(name = "season", defaultValue = "-1") int season) {
        return matchRepository.mostGoalsScored(season);
    }

    @QueryMapping
    @GetMapping("/season-performance")
    public Flux<EplStanding> seasonPerformance(
            @Argument(name = "team") @RequestParam(name = "team") String team,
            @Argument(name = "season") @RequestParam(name = "season", defaultValue = "-1") int season) {
        return matchRepository.seasonPerformance(team, season);
    }

    @QueryMapping
    @GetMapping("/season-table")
    public Flux<EplStanding> seasonTable(
            @Argument(name = "season") @RequestParam(name = "season") int season) {
        return matchRepository.seasonTable(season);
    }

    @QueryMapping
    @GetMapping("/winners")
    public Flux<EplStanding> winner(
            @Argument(name="season") @RequestParam(name="season", defaultValue = "-1") int season
    ) {
        return matchRepository.winner(season);
    }

    @QueryMapping
    @GetMapping("/all-teams")
    public Flux<String> allTeams(
            @Argument(name="season") @RequestParam(name="season", defaultValue = "-1") int season
    ) {
        return matchRepository.allTeams(season);
    }
}
