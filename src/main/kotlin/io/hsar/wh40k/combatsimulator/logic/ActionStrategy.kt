package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

interface ActionStrategy {
    fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActions: Collection<TurnAction>): List<TurnAction>
}