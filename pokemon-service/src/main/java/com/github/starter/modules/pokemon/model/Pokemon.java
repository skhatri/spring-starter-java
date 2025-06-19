package com.github.starter.modules.pokemon.model;

import java.util.Map;
import java.util.Objects;



public final class Pokemon {
    private final int pokedex;
    private final String name;
    private final String primaryType;
    private final String secondaryType;
    private final int total;
    private final int hp;
    private final int attack;
    private final int defence;
    private final int spAttack;
    private final int spDefence;
    private final int speed;
    private final int generation;
    private final String legendary;
    private final String region;
    
    private Effectiveness effectiveness;
    

    public Pokemon(String name, int total, String primaryType, String secondaryType, int pokedex,
                   String legendary, int hp, int attack, int defence, int spAttack, int spDefence,
                   int speed, int generation, String region) {
        if (region == null || region.isEmpty()) {
            throw new IllegalArgumentException("Region must be provided");
        }
        this.name = name;
        this.total = total;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
        this.pokedex = pokedex;
        this.legendary = legendary;
        this.hp = hp;
        this.attack = attack;
        this.defence = defence;
        this.spAttack = spAttack;
        this.spDefence = spDefence;
        this.speed = speed;
        this.generation = generation;
        this.region = region;
    }

    public static Pokemon fromMap(Map<String, Object> kv) {
        return new Pokemon(
                (String) kv.get("name"),
                (Integer) kv.get("total"),
                (String) kv.get("primary_type"),
                (String) kv.get("secondary_type"),
                (Integer) kv.get("pokedex"),
                (String) kv.get("legendary"),
                (Integer) kv.get("hp"),
                (Integer) kv.get("attack"),
                (Integer) kv.get("defence"),
                (Integer) kv.get("sp_attack"),
                (Integer) kv.get("sp_defence"),
                (Integer) kv.get("speed"),
                (Integer) kv.get("generation"),
                (String) kv.get("region")
        );
    }

    public static Pokemon fromReadable(io.r2dbc.spi.Readable kv) {
        String name = (String) kv.get("name");
        Integer total = (Integer) kv.get("total");
        String primaryType = (String) kv.get("primary_type");
        String secondaryType = (String) kv.get("secondary_type");
        Integer pokedex = (Integer) kv.get("pokedex");
        String legendary = (String) kv.get("legendary");
        Integer hp = (Integer) kv.get("hp");
        Integer attack = (Integer) kv.get("attack");
        Integer defence = (Integer) kv.get("defence");
        Integer spAttack = (Integer) kv.get("sp_attack");
        Integer spDefence = (Integer) kv.get("sp_defence");
        Integer speed = (Integer) kv.get("speed");
        Integer generation = (Integer) kv.get("generation");
        String region = (String) kv.get("region");
        
        return new Pokemon(
                name != null ? name : "Unknown",
                total != null ? total : 0,
                primaryType != null ? primaryType : "Normal",
                secondaryType,
                pokedex != null ? pokedex : 0,
                legendary != null ? legendary : "False",
                hp != null ? hp : 0,
                attack != null ? attack : 0,
                defence != null ? defence : 0,
                spAttack != null ? spAttack : 0,
                spDefence != null ? spDefence : 0,
                speed != null ? speed : 0,
                generation != null ? generation : 1,
                region != null ? region : "Unknown"
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokemon pokemon)) return false;
        return pokedex == pokemon.pokedex &&
                total == pokemon.total &&
                hp == pokemon.hp &&
                attack == pokemon.attack &&
                defence == pokemon.defence &&
                spAttack == pokemon.spAttack &&
                spDefence == pokemon.spDefence &&
                speed == pokemon.speed &&
                generation == pokemon.generation &&
                Objects.equals(name, pokemon.name) &&
                Objects.equals(primaryType, pokemon.primaryType) &&
                Objects.equals(secondaryType, pokemon.secondaryType) &&
                Objects.equals(legendary, pokemon.legendary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pokedex, name, primaryType, secondaryType, total, hp, attack, defence,
                           spAttack, spDefence, speed, generation, legendary);
    }
    public int getPokedex() { return pokedex; }
    public String getName() { return name; }
    public String getPrimaryType() { return primaryType; }
    public String getSecondaryType() { return secondaryType; }
    public int getTotal() { return total; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefence() { 
        return defence; 
    }
    public int getSpAttack() { 
        return spAttack; 
    }
    public int getSpDefence() { 
        return spDefence; 
    }
    public int getSpeed() { return speed; }
    public int getGeneration() { return generation; }
    public String getLegendary() { return legendary; }
    public String getRegion() { return region; }

    public Effectiveness getEffectiveness() {
        return effectiveness;
    }
    public void setEffectiveness(Effectiveness effectiveness) {
        this.effectiveness = effectiveness;
    }


}
