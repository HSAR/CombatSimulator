package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus
import kotlin.math.absoluteValue
import kotlin.math.max

data class World(val friendlyForces: MutableList<UnitInstance>, val enemyForces: MutableList<UnitInstance>,
                 val unitPositions: MutableMap<UnitInstance, MapPosition>) {

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

    // Used by things like TacticalActionStrategy to work out how far away units are from each other
    // This will give the inclusive distance from square to square, so callers need to subtract 1 when working out
    // the distance they need to travel to be NEXT to a unit
    fun distanceApart(unit: UnitInstance, otherUnit: UnitInstance): Int {
        return unitPositions.getValue(unit)
                .distanceToPosition(unitPositions.getValue(otherUnit))
    }

    fun canMoveToUnit(unit: UnitInstance, otherUnit: UnitInstance, moveType: TurnAction.MoveAction): Boolean {
        return (distanceApart(unit, otherUnit) - 1 <=
                unit.unit.stats.baseStats.getValue(BaseStat.AGILITY).getBonus()
                        .let { bonus ->
                            moveType.getMovementRange(bonus)
                        })
                && moveType.isValidMovementPath(unitPositions.getValue(unit), unitPositions.getValue(otherUnit))
    }
}

data class MapPosition(val x: Int, val y: Int) {

    fun distanceToPosition(otherPosition: MapPosition): Int {
        // return distance to other position in metres
        // as can diagonal move for 1, this equates to the distance in the longest cartesian direction
        return max((this.x - otherPosition.x).absoluteValue, (this.y - otherPosition.y).absoluteValue)
    }

    operator fun minus(otherPosition: MapPosition): Int {
        return this.distanceToPosition(otherPosition)
    }
}