package com.github.starter.modules.epl.model;

public class EplStanding {
    private int season;
    private int ranking;
    private String team;
    private int played;
    private int gf;
    private int ga;
    private int gd;
    private int points;

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getGf() {
        return gf;
    }

    public void setGf(int gf) {
        this.gf = gf;
    }

    public int getGa() {
        return ga;
    }

    public void setGa(int ga) {
        this.ga = ga;
    }

    public int getGd() {
        return gd;
    }

    public void setGd(int gd) {
        this.gd = gd;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
