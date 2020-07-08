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
import kotlin.math.abs
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
                calcMovementRange(unit, moveType))
                && moveType.isValidMovementPath(unitPositions.getValue(unit), unitPositions.getValue(otherUnit))
    }

    fun moveTowards(unit: UnitInstance, otherUnit: UnitInstance, maxMovement: Int): Unit {
        //TODO need to work out whether to return a new position or actually update the unit's position

        val movementRange = calcMovementRange(unit, moveType)
        val unitPosition = getPosition(unit)
        val otherPosition = getPosition(otherUnit)
        val deltaX = otherPosition.x - unitPosition.x
        val deltaY = otherPosition.y - unitPosition.y


        var workingPosition = MapPosition(getPosition(unit))
        var tempPosition
        var movementLeft = movementRange
        while(movementLeft > 0) {
            // work out which direction to move in
            val direction = getDirection(deltaX, deltaY)

            //check to see if anything in that space
            val spaceContents = getSpaceContents(getPosition(unit) + direction)
            when(spaceContents) {
                null -> TODO()  // update working position AND actual position
                in getAllies(unit) -> TODO() // will take two moves to move through. Only update workingposition
                // Need to think about position caching as could try to move through ally and no space on other side
                in getAdversaries(unit) -> TODO() //stop goind
                else -> TODO()
            }
        }

        // move as far diagonally as possible
        val diagDistance = minOf(abs(deltaX), abs(deltaY))
        // TODO fix movement to have a more thorough implementation that checks for whether space is free etc
    }

    fun getDirection(deltaX: Int, deltaY: Int): MapPosition {
        val xDir = when {
            deltaX > 0 -> 1
            deltaX < 0 -> -1
            else -> 0
        }

        val yDir = when {
            deltaY > 0 -> 1
            deltaY < 0 -> -1
            else -> 0
        }
        return MapPosition(xDir, yDir)

    }

    fun improvePosition(unit: UnitInstance, moveType: MoveAction) {
        //TODO
        // Initial implementation is just to find closest adversary and move towards it
        val closestAdversary = getAdversaries(unit).minBy { adversary ->
            distanceApart(unit, adversary)
        }!!
        moveTowards(unit, closestAdversary, moveType)
    }

    fun calcMovementRange(unit: UnitInstance, moveType: MoveAction): Int {
        return unit.unit.stats.baseStats.getValue(BaseStat.AGILITY).getBonus()
                .let { bonus ->
                    moveType.getMovementRange(bonus)
                }
    }

    fun getAdversaries(unit: UnitInstance): List<UnitInstance> {
        return if(unit in this.friendlyForces) {
            enemyForces
        } else {
            friendlyForces
        }
    }

    fun getAllies(unit: UnitInstance): List<UnitInstance> {
        return if(unit in this.friendlyForces) {
            friendlyForces
        } else {
            enemyForces
        }
    }

    fun getPosition(unit: UnitInstance): MapPosition {
        return this.unitPositions.getValue(unit)
    }

    fun getSpaceContents(mapPosition: MapPosition): UnitInstance? {
        val matchingUnits = unitPositions.filter { it.value == mapPosition }.toList()
        // use overloaded == operator to check x & y are same
        return when {
            matchingUnits.isEmpty() -> null
            else -> matchingUnits.first().first // Unitinstance in square
        }
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

    fun removeUnits(unitsToRemove: List<UnitInstance>) {
        friendlyForces.removeAll(unitsToRemove)
        enemyForces.removeAll(unitsToRemove)
    }

    fun isInMeleeRange(user: UnitInstance, target: UnitInstance): Boolean {
        return distanceApart(user, target) == 1
    }
}

data class MapPosition(val x: Int, val y: Int) {

    constructor(otherPosition: MapPosition): this(otherPosition.x, otherPosition.y)
    /**
     * Returns the distance to the other position in metres, as diagonal moves count as 1 distance.
     * This equates to the distance in the longest cartesian direction
     */
    fun distanceToPosition(otherPosition: MapPosition): Int =
            max((this.x - otherPosition.x).absoluteValue, (this.y - otherPosition.y).absoluteValue)

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is MapPosition -> this.x == other.x && this.y == other.y
            else -> false
        }
    }

    operator fun minus(otherPosition: MapPosition): Int {
        return this.distanceToPosition(otherPosition)
    }

    operator fun plus(otherPosition: MapPosition): MapPosition {
        return MapPosition(this.x + otherPosition.x, this.y + otherPosition.y)
    }
}