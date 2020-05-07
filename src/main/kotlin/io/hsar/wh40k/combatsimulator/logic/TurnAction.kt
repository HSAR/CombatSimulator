package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.unit.Bonus
import io.hsar.wh40k.combatsimulator.model.unit.Bonus.*

sealed class TurnAction {
    abstract val actionCost: ActionCost

    data class MeleeAttack(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SingleRangedAttack(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SemiAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class FullAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class WeaponReload(override val actionCost: ActionCost) : TurnAction()

    object HalfAim: BonusCausingAction, TurnAction() {
        override val actionCost =  HALF_ACTION
        override val appliesBonus = BONUS_AIM_HALF
    }

    object FullAim: BonusCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val appliesBonus = BONUS_AIM_FULL
    }

    abstract class MoveAction: TurnAction() {
        abstract fun getMovementRange(agilityBonus: Short): Short
        abstract fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
    }

    object HalfMove: MoveAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override fun getMovementRange(agilityBonus: Short): Short {
            return agilityBonus
        }
        override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
            return true  // unlike charge etc, there are no special restrictions on half move pathing
        }
    }

    data class ChargeAttack(override val damage: String): DamageCausingAction, MoveAction() {

        override val actionCost: ActionCost = FULL_ACTION
        override val numberOfAttacks = 1

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

interface BonusCausingAction {
    val appliesBonus: Bonus
}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}