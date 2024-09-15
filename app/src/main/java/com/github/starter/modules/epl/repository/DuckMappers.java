package com.github.starter.modules.epl.repository;

import com.github.starter.modules.epl.model.EplMatch;
import com.github.starter.modules.epl.model.EplStanding;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

class DuckMappers {

    private DuckMappers() {

    }

    static Function<Map<String, Object>, EplMatch> eplMatch() {
        return kv -> {
            EplMatch eplMatch = new EplMatch();
            eplMatch.setSeason((int) kv.get("season"));
            eplMatch.setTeam((String) kv.get("team"));
            eplMatch.setWk((int) kv.get("wk"));
            eplMatch.setMatchDate((LocalDate) kv.get("matchDate"));
            eplMatch.setPoints((int) kv.get("points"));
            eplMatch.setGf((int) kv.get("gf"));
            eplMatch.setGa((int) kv.get("ga"));
            eplMatch.setResult((String) kv.get("result"));
            eplMatch.setOpponent((String) kv.get("opponent"));
            eplMatch.setVenue((String) kv.get("venue"));
            return eplMatch;
        };
    }

    static Function<Map<String, Object>, EplStanding> eplStanding() {
        return kv -> {
            EplStanding standing = new EplStanding();
            standing.setSeason((int) kv.get("season"));
            standing.setPlayed((int) kv.get("played"));
            standing.setPoints((int) kv.get("points"));
            standing.setTeam((String) kv.get("team"));

            standing.setRanking((int) kv.get("ranking"));
            standing.setGf((int) kv.get("gf"));
            standing.setGa((int) kv.get("ga"));
            standing.setGd((int) kv.get("gd"));
            return standing;
        };
    }

    static <T> Function<Map<String, Object>, T> field(String key) {
        return kv -> (T) kv.get(key);
    }
}
