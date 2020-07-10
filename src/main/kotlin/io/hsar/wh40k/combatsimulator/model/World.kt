package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.MoveAction
import io.hsar.wh40k.combatsimulator.logic.TargetedAction

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

    fun moveTowards(unit: UnitInstance, otherUnit: UnitInstance, maxMovement: Int): Unit {
        
        val unitPosition = getPosition(unit)
        val otherPosition = getPosition(otherUnit)
        val deltaX = otherPosition.x - unitPosition.x
        val deltaY = otherPosition.y - unitPosition.y

        var workingPosition = MapPosition(unitPosition)
        var tempPosition = MapPosition(unitPosition)
        var movementLeft = maxMovement
        var squaresMoved = 0
        while(movementLeft > 0) {
            // work out which direction to move in
            val direction = getDirection(deltaX, deltaY)

            //check to see if anything in that space
            when(getSpaceContents(getPosition(unit) + direction)) {
                null ->  {  // free to use space
                    if(squaresMoved <= movementLeft) {
                        workingPosition = tempPosition + direction
                        tempPosition = MapPosition(unitPosition)
                        movementLeft -= squaresMoved
                        squaresMoved = 0
                    } else {
                        movementLeft = 0 // can't reach the square with remaining movement
                    }

                }
                in getAllies(unit) -> {
                    tempPosition = tempPosition + direction
                    squaresMoved += 1
                } // will take two moves to move through. Only update workingposition
                // Need to think about position caching as could try to move through ally and no space on other side
                in getAdversaries(unit) -> movementLeft = 0 //stop going
                else -> throw RuntimeException("Square neither empty or occupied")
            }
        }
        setPosition(unit, workingPosition)
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

    fun setPosition(unit: UnitInstance, position: MapPosition) {
        this.unitPositions[unit] = position
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

    fun createCopy(): World {
        val tempUnitPositions = unitPositions.mapValues { it ->
            MapPosition(it.value)
        }.toMutableMap()
        return World(friendlyForces, enemyForces, tempUnitPositions)
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