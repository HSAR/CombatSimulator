package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition

sealed class TurnAction {
    abstract val actionCost: ActionCost

    data class ATTACK_MELEE(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class ATTACK_RANGED_SINGLE(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class ATTACK_RANGED_SEMI_AUTO_BURST(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class ATTACK_RANGED_FULL_AUTO_BURST(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class RELOAD_WEAPON(override val actionCost: ActionCost) : TurnAction()

    abstract class  MoveAction: TurnAction() {
        abstract fun getMovementRange(agilityBonus: Short): Short
        abstract fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
    }

    object HalfMoveAction: MoveAction() {
        override val actionCost: ActionCost = ActionCost.HALF_ACTION
        override fun getMovementRange(agilityBonus: Short): Short {
            return agilityBonus
        }
        override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
            return true  // unlike charge etc, there are no special restrictions on half move pathing
        }
    }

    object ChargeAction: MoveAction() {
        override val actionCost: ActionCost = ActionCost.FULL_ACTION
        override fun getMovementRange(agilityBonus: Short): Short {
            return (3 * agilityBonus).toShort()
        }
        override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
            return startPoint - endPoint >= 4
        }
    }
}

interface DamageCausingAction {
    val damage: String
    val numberOfAttacks: Int
}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}