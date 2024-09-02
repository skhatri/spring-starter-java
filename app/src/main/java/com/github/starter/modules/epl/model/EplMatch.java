package com.github.starter.modules.epl.model;


import java.util.Date;

public class EplMatch {

    private int season;
    private int wk;
    private Date matchDate;
    private String team;
    private String opponent;
    private String venue;
    private String result;
    private int gf;
    private int ga;
    private int points;

    public void setSeason(int season) {
        this.season = season;
    }

    public void setWk(int wk) {
        this.wk = wk;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setGf(int gf) {
        this.gf = gf;
    }

    public void setGa(int ga) {
        this.ga = ga;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getSeason() {
        return season;
    }

    public int getWk() {
        return wk;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public String getTeam() {
        return team;
    }

    public String getOpponent() {
        return opponent;
    }

    public String getVenue() {
        return venue;
    }

    public String getResult() {
        return result;
    }

    public int getGf() {
        return gf;
    }

    public int getGa() {
        return ga;
    }

    public int getPoints() {
        return points;
    }
}
