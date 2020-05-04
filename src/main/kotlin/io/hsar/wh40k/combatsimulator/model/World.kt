package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.TurnAction

data class World(val friendlyForces: List<UnitInstance>, val enemyForces: List<UnitInstance>) {

    fun executeActions(executingUnit: UnitInstance, actionsToExecute: List<TurnAction>) {
        actionsToExecute
                .map { actionToExecute ->
                    TODO("Not yet implemented")
                }
    }
}