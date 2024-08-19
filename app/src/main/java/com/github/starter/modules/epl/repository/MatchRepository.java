package com.github.starter.modules.epl.repository;

import com.github.starter.modules.epl.model.EplMatch;
import com.github.starter.modules.epl.model.EplStanding;
import io.r2dbc.spi.Readable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.function.Function;

@Repository
public class MatchRepository {

    private final DatabaseClient client;

    @Autowired
    public MatchRepository(DatabaseClient client) {
        this.client = client;
    }

    public Flux<EplMatch> biggestMargin(int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return client.sql(String.format("""
                        SELECT * 
                        FROM pokedex.epl_team_match 
                        where season %s AND points > 0 and abs(gf-ga)=(
                            select max(abs(gf-ga)) 
                            from pokedex.epl_team_match
                            where season %s
                        ) 
                        """, seasonSelector, seasonSelector)).map(Mappers.eplMatch())
                .all();
    }

    public Flux<EplMatch> mostGoalsScored(int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return client.sql(String.format("""
                        SELECT * 
                        FROM pokedex.epl_team_match 
                        where season %s AND points > 0 and gf=(
                            select max(gf) 
                            from pokedex.epl_team_match
                            where season %s
                        ) 
                        """, seasonSelector, seasonSelector)).map(Mappers.eplMatch())
                .all();
    }


    public Flux<EplStanding> seasonPerformance(String team, int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return client.sql(String.format("""
                        SELECT *
                        FROM pokedex.epl_standings
                        where season %s AND team = $1
                        order by season asc
                        """, seasonSelector)).bind("$1", team).map(Mappers.eplStanding())
                .all();
    }

    public Flux<EplStanding> seasonTable(int season) {

        return client.sql("""
                        SELECT *
                        FROM pokedex.epl_standings
                        where season = $1 
                        order by ranking asc
                        """).bind("$1", season).map(Mappers.eplStanding())
                .all();
    }

    public Flux<EplStanding> winner(int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return client.sql(String.format("""
                        SELECT *
                        FROM pokedex.epl_standings
                        where season %s AND ranking = 1
                        order by season asc
                        """, seasonSelector)).map(Mappers.eplStanding())
                .all();
    }

    public Flux<String> allTeams(int season) {
        String seasonSelector = "<> -1";
        if (season != -1) {
            seasonSelector = String.format("= %d", season);
        }
        return client.sql(String.format("""
                        SELECT distinct team
                        FROM pokedex.epl_standings
                        where season %s
                        order by team asc
                        """, seasonSelector)).map(Mappers.field("team", String.class))
                .all();
    }
}
