package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max

data class World(
        val friendlyForces: MutableList<UnitInstance>,
        val enemyForces: MutableList<UnitInstance>,
        val unitPositions: MutableMap<UnitInstance, MapPosition>
) {
     private var isLoggable = true
    /**
     * Used by things like TacticalActionStrategy to work out how far away units are from each other
     * This will give the inclusive distance from square to square, so callers need to subtract 1 when working out
     * the distance they need to travel to be NEXT to a unit
     */
    fun distanceApart(unit: UnitInstance, otherUnit: UnitInstance): Int {
        return unitPositions.getValue(unit)
                .distanceToPosition(unitPositions.getValue(otherUnit))
    }

    /*
    Given a user and another UnitInstance that the user wants to move towards, this will handle the low level
    mechanics of moving the unit as close as it can get for its movement range, stoppign if it comes within melee
    distance
     */
    fun moveTowards(unit: UnitInstance, otherUnit: UnitInstance, maxMovement: Int): Unit {
        if(unit === otherUnit) {
            throw IllegalArgumentException("A UnitInstance cannot move towards itself")
        }
        val unitPosition = getPosition(unit)
        val otherPosition = getPosition(otherUnit)

        var workingPosition = MapPosition(unitPosition)
        var tempPosition = MapPosition(unitPosition)
        var movementLeft = maxMovement
        var squaresMoved = 0
        while(movementLeft > 0) {
            // work out which direction to move in
            val deltaX = otherPosition.x - tempPosition.x
            val deltaY = otherPosition.y - tempPosition.y
            if(abs(deltaX) <=1 && abs(deltaY) <=1) {
                break // we are in square next to unit
            }
            val direction = getDirection(deltaX, deltaY)

            //check to see if anything in that space
            when(getSpaceContents(tempPosition + direction)) {
                null ->  {  // free to use space
                    if(squaresMoved + 1 <= movementLeft) {
                        workingPosition = tempPosition + direction
                        tempPosition = MapPosition(workingPosition)
                        movementLeft -= squaresMoved + 1
                        squaresMoved = 0
                    } else {
                        movementLeft = 0 // can't reach the square with remaining movement
                    }

                }
                in getAllies(unit) -> {
                    tempPosition += direction
                    squaresMoved += 2
                } // will take two moves to move through. Only update workingposition
                // Need to think about position caching as could try to move through ally and no space on other side
                in getAdversaries(unit) -> movementLeft = 0 //stop going
                else -> throw RuntimeException("Square neither empty or occupied")
            }
        }
        setPosition(unit, workingPosition)
    }

    /*
    Used by moveTowards to work out which direction to move a step in on the cartesian plane based on the vector to
    the target
     */
    private fun getDirection(deltaX: Int, deltaY: Int): MapPosition {
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

    private fun getSpaceContents(mapPosition: MapPosition): UnitInstance? {
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

    /*
    This returns a copy of the world that can be updated without affecting the original world.
    This is used during combat logic when applying actions to see what the best combination is

    Caution - to update a UnitInstance inside the copy of the world, you still need to call replaceWithCopy first
    to work on a deep copy of that unit instance, as this only clones the containers, not their contents

    This clones the unitPositions and copy the forces lists into new list objects so that if we then swap
    out entries in the copies of the lists, they will not be swapped out in the original lists
     */
    fun createCopy(): World {
         val tempUnitPositions = unitPositions.mapValues { unitPosition ->
            MapPosition(unitPosition.value)
         }.toMutableMap()
         val tempWorld = World(friendlyForces.toMutableList(), enemyForces.toMutableList(), tempUnitPositions)
        tempWorld.isLoggable = false // don't want to log actions when applying them as part of EV calculation
        return tempWorld
    }

    fun replaceUnitInstanceWithCopy(unitInstance: UnitInstance): UnitInstance {
        val tempUnitInstance = unitInstance.createCopy()
        when (unitInstance) {
            in friendlyForces -> {
                friendlyForces[friendlyForces.indexOf(unitInstance)] = tempUnitInstance
            }
            in enemyForces -> {
                enemyForces[enemyForces.indexOf(unitInstance)] = tempUnitInstance
            }
            else -> {
                // do nothing as this unitinstance is not in the world (may be an existing copy)
            }
        }

        // replace unit with the copy in unitPositions so that the references line
        // up for doing logic on it
        val position = unitPositions[unitInstance]
        if(position != null) {
            unitPositions.remove(unitInstance)
            unitPositions[tempUnitInstance] = position
        }
        return tempUnitInstance
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