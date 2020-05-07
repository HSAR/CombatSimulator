package io.hsar.wh40k.combatsimulator.model.unit

data class Stats(val baseStats: Map<BaseStat, Int>, val derivedStats: Map<DerivedStats, Int>)

/**
 * Character base statistics.
 * Some abilities not related to combat are not represented.
 */
enum class BaseStat {
    MAX_HEALTH,
    WEAPON_SKILL,
    BALLISTIC_SKILL,
    STRENGTH,
    TOUGHNESS,
    AGILITY,
    INTELLIGENCE,
    WILLPOWER,
    FELLOWSHIP
}

/**
 * Character skills defined in relation to base statistics.
 * Some abilities not related to combat are not represented.
 */
// #TODO Pre-compute using input information about training
enum class DerivedStats {
    DODGE,
    PARRY
}

object StatUtils {
    fun Int.getBonus(): Int = (this / 10)
}