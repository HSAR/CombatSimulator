package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.*
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
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
                        is TargetedAction -> {
                            when(actionToExecute.action) {
                                is DamageCausingAction -> {
                                    AttackExecution.rollHits(
                                            executingUnit,
                                            actionToExecute.target,
                                            actionToExecute.action as DamageCausingAction
                                            // forced to hard cast to avoid compiler error
                                    ).let { numberOfHits ->
                                        repeat(numberOfHits) {
                                            actionToExecute.target.receiveDamage(
                                                    AttackExecution.calcDamage(
                                                            executingUnit,
                                                            actionToExecute.target,
                                                            actionToExecute.action as DamageCausingAction))
                                        }
                                    }
                                }
                                else -> TODO()
                            }



                        }
                        else -> TODO("Not yet implemented")
                    }
                }
    }

    /**
     * Used by things like TacticalActionStrategy to work out how far away units are from each other
     * This will give the inclusive distance from square to square, so callers need to subtract 1 when working out
     * the distance they need to travel to be NEXT to a unit
     */
    fun distanceApart(unit: UnitInstance, otherUnit: UnitInstance): Int {
        return unitPositions.getValue(unit)
                .distanceToPosition(unitPositions.getValue(otherUnit))
    }

    fun canMoveToUnit(unit: UnitInstance, otherUnit: UnitInstance, moveType: MoveAction): Boolean {
        return (distanceApart(unit, otherUnit) - 1 <=
                unit.unit.stats.baseStats.getValue(BaseStat.AGILITY).getBonus()
                        .let { bonus ->
                            moveType.getMovementRange(bonus)
                        })
                && moveType.isValidMovementPath(unitPositions.getValue(unit), unitPositions.getValue(otherUnit))
    }

    fun getAdversaries(unit: UnitInstance): List<UnitInstance> {
        return if(unit in this.friendlyForces) {
            enemyForces
        } else {
            friendlyForces
        }
    }

    fun getPosition(unit: UnitInstance): MapPosition {
        return this.unitPositions.getValue(unit)
    }
}

data class MapPosition(val x: Int, val y: Int) {

    /**
     * Returns the distance to the other position in metres, as diagonal moves count as 1 distance.
     * This equates to the distance in the longest cartesian direction
     */
    fun distanceToPosition(otherPosition: MapPosition): Int =
            max((this.x - otherPosition.x).absoluteValue, (this.y - otherPosition.y).absoluteValue)

    operator fun minus(otherPosition: MapPosition): Int {
        return this.distanceToPosition(otherPosition)
    }
}