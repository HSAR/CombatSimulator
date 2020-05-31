package io.hsar.wh40k.combatsimulator

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue

enum class Winner { FRIENDLY, ENEMY }

data class UnitSummary(val name: String, val currentHealth: Int) {
    constructor(unitInstance: UnitInstance) : this(name = unitInstance.name, currentHealth = (unitInstance.currentAttributes.getValue(Attribute.CURRENT_HEALTH) as NumericValue).value)
}

data class SimulationResult(val winner: Winner, val rounds: Int, val remainingUnits: List<UnitSummary>)