package io.hsar.wh40k.combatsimulator.model

interface  MoveAction {
    fun getMovementRange(agilityBonus: Short): Short
    fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
}

object HalfMoveAction: MoveAction {
    override fun getMovementRange(agilityBonus: Short): Short {
        return agilityBonus
    }
    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return true  // unlike charge etc, there are no special restrictions on half move pathing
    }
}

object ChargeAction: MoveAction {
    override fun getMovementRange(agilityBonus: Short): Short {
        return (3 * agilityBonus).toShort()
    }
    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return startPoint - endPoint >= 4
    }
}