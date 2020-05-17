package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.EffectCausingAction
import io.hsar.wh40k.combatsimulator.logic.MoveAction
import io.hsar.wh40k.combatsimulator.logic.TargetedAction
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus
import kotlin.math.absoluteValue
import kotlin.math.max

data class World(
        val friendlyForces: MutableList<UnitInstance>,
        val enemyForces: MutableList<UnitInstance>,
        val unitPositions: MutableMap<UnitInstance, MapPosition>
) {

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

    fun findDead(): List<UnitInstance> {
        return listOf(findDeadInternal(friendlyForces),findDeadInternal(enemyForces)).flatten()
    }

    private fun findDeadInternal(unitList: MutableList<UnitInstance>): List<UnitInstance> {
        return unitList.filter { unitInstance ->
            when(val health = unitInstance.currentAttributes[Attribute.CURRENT_HEALTH]) {
                is NumericValue -> health.value <= 0  // simplification
                else -> throw RuntimeException("Current health must be a NumericValue")
            }
        }
    }

    fun removeDead() {
        val deadUnits = findDead()
        friendlyForces.removeAll(deadUnits)
        enemyForces.removeAll(deadUnits)
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