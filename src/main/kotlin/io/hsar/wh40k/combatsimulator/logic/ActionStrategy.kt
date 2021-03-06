package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.actionoptions.ActionOption
import io.hsar.wh40k.combatsimulator.logic.actionoptions.TargetedAction
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

interface ActionStrategy {
    fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TargetedAction>
}