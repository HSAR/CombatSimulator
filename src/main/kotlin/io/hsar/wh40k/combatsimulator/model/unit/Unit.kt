package io.hsar.wh40k.combatsimulator.model.unit

/**
 * A unit to take part in combat.
 * This class should not contain information that can change during the course of simulation.
 */
data class Unit(
        val name: String,
        val description: String,
        val stats: Stats
)