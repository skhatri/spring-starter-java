package com.github.pokemon.model

import java.math.BigDecimal

data class Pokemon(
    val name: String, val baseStat: Int,
    val primaryType: String, val secondaryType: String?, val location: String, val legendary: Boolean?,
    val weakness: Array<String>, val height: BigDecimal, val weight: BigDecimal
) {

    companion object {
        fun fromMap(kv: Map<String, Any?>): Pokemon {
            return Pokemon(
                kv["name"] as String, baseStat = kv["base_stat"] as Int,
                primaryType = kv["primary_type"] as String,
                secondaryType = kv["secondary_type"] as String?,
                location = kv["location"] as String,
                legendary = kv["legendary"] as Boolean?,
                weakness =kv["weakness"] as Array<String>,
                height = kv["height"] as BigDecimal,
                weight = kv["weight"] as BigDecimal
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pokemon

        if (name != other.name) return false
        if (baseStat != other.baseStat) return false
        if (primaryType != other.primaryType) return false
        if (secondaryType != other.secondaryType) return false
        if (location != other.location) return false
        if (legendary != other.legendary) return false
        if (!weakness.contentEquals(other.weakness)) return false
        if (height != other.height) return false
        if (weight != other.weight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + baseStat
        result = 31 * result + primaryType.hashCode()
        result = 31 * result + secondaryType.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + legendary.hashCode()
        result = 31 * result + weakness.contentHashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + weight.hashCode()
        return result
    }
}