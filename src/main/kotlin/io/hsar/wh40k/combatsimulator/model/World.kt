package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import java.lang.IllegalStateException

data class World(val friendlyForces: MutableList<UnitInstance>, val enemyForces: MutableList<UnitInstance>) {

    fun executeActions(executingUnit: UnitInstance, actionsToExecute: List<TurnAction>) {
        // #TODO: Check total
        // #TODO: Check range
        actionsToExecute
                .map { actionToExecute ->
                    when (actionToExecute) {
                        is DamageCausingAction -> {
                            // #TODO Move target selection somewhere better
                            // #TODO Make target selection not shit
                            val targetUnit = when (executingUnit) {
                                in friendlyForces -> enemyForces.random()
                                in enemyForces -> friendlyForces.random()
                                else -> throw IllegalStateException("Executing turn for a unit that is not on any side.")
                            }


                        }
                        else -> TODO("Not yet implemented")
                    }
                }
    }
}