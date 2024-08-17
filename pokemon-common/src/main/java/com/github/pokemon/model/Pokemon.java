package com.github.pokemon.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Pokemon {

    private final String name;
    private final int baseStat;
    private final String primaryType;
    private final String secondaryType;
    private final String location;
    private final Boolean legendary;
    private final String[] weakness;
    private final BigDecimal height;
    private final BigDecimal weight;

    public Pokemon(String name, int baseStat, String primaryType, String secondaryType, String location,
                   Boolean legendary, String[] weakness, BigDecimal height, BigDecimal weight) {
        this.name = name;
        this.baseStat = baseStat;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
        this.location = location;
        this.legendary = legendary;
        this.weakness = weakness;
        this.height = height;
        this.weight = weight;
    }

    public static Pokemon fromMap(Map<String, Object> kv) {
        return new Pokemon(
                (String) kv.get("name"),
                (Integer) kv.get("base_stat"),
                (String) kv.get("primary_type"),
                (String) kv.get("secondary_type"),
                (String) kv.get("location"),
                (Boolean) kv.get("legendary"),
                (String[]) kv.get("weakness"),
                (BigDecimal) kv.get("height"),
                (BigDecimal) kv.get("weight")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pokemon pokemon = (Pokemon) o;
        return baseStat == pokemon.baseStat &&
                Objects.equals(name, pokemon.name) &&
                Objects.equals(primaryType, pokemon.primaryType) &&
                Objects.equals(secondaryType, pokemon.secondaryType) &&
                Objects.equals(location, pokemon.location) &&
                Objects.equals(legendary, pokemon.legendary) &&
                Arrays.equals(weakness, pokemon.weakness) &&
                Objects.equals(height, pokemon.height) &&
                Objects.equals(weight, pokemon.weight);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, baseStat, primaryType, secondaryType, location, legendary, height, weight);
        result = 31 * result + Arrays.hashCode(weakness);
        return result;
    }

    // Getters for all fields (optional, depending on your use case)
    public String getName() { return name; }
    public int getBaseStat() { return baseStat; }
    public String getPrimaryType() { return primaryType; }
    public String getSecondaryType() { return secondaryType; }
    public String getLocation() { return location; }
    public Boolean getLegendary() { return legendary; }
    public String[] getWeakness() { return weakness; }
    public BigDecimal getHeight() { return height; }
    public BigDecimal getWeight() { return weight; }
}
