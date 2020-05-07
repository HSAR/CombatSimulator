package io.hsar.wh40k.combatsimulator.model

interface  MoveAction {
    fun getMovementRange(agilityBonus: Short): Short
    fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
}
