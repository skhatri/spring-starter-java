package com.github.starter.modules.epl.repository;

import com.github.starter.config.duck.DataClient;
import com.github.starter.modules.epl.model.EplMatch;
import com.github.starter.modules.epl.model.EplStanding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "duckdb")
@Repository
public class DuckMatchRepository implements MatchRepository {

    private final DataClient client;

    @Autowired
    public DuckMatchRepository(DataClient client) {
        this.client = client;
    }

    public Flux<EplMatch> biggestMargin(int season) {
        String seasonSelector = getSeasonSelector(season);
        return client.sql(String.format("""
                SELECT *
                FROM epl_team_match
                where season %s AND points > 0 and abs(gf-ga)=(
                    select max(abs(gf-ga))
                    from epl_team_match
                    where season %s
                )
                """, seasonSelector, seasonSelector)).map(DuckMappers.eplMatch());
    }

    public Flux<EplMatch> mostGoalsScored(int season) {
        String seasonSelector = getSeasonSelector(season);
        return client.sql(String.format("""
                        SELECT *
                        FROM epl_team_match
                        where season %s AND points > 0 and gf=(
                            select max(gf)
                            from epl_team_match
                            where season %s
                        )
                        """, seasonSelector, seasonSelector)).map(DuckMappers.eplMatch());
    }


    public Flux<EplStanding> seasonPerformance(String team, int season) {
        String seasonSelector = getSeasonSelector(season);
        return client.sql(String.format("""
                        SELECT *
                        FROM epl_standings
                        where season %s AND team = ?
                        order by season asc
                        """, seasonSelector)).bind(1, team).map(DuckMappers.eplStanding());
    }

    public Flux<EplStanding> seasonTable(int season) {

        return client.sql("""
                        SELECT *
                        FROM pokedex.epl_standings
                        where season = ?
                        order by ranking asc
                        """).bind(1, season).map(DuckMappers.eplStanding());
    }

    public Flux<EplStanding> winner(int season) {
        String seasonSelector = getSeasonSelector(season);
        return client.sql(String.format("""
                        SELECT *
                        FROM epl_standings
                        where season %s AND ranking = 1
                        order by season asc
                        """, seasonSelector)).map(DuckMappers.eplStanding());
    }

    public Flux<String> allTeams(int season) {
        String seasonSelector = getSeasonSelector(season);
        return client.sql(String.format("""
                        SELECT distinct team
                        FROM epl_standings
                        where season %s
                        order by team asc
                        """, seasonSelector)).map(DuckMappers.field("team"));
    }

    private static String getSeasonSelector(int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return seasonSelector;
    }
}
