package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.Effects
import io.hsar.wh40k.combatsimulator.model.unit.Effects.*

sealed class ActionOption {
    abstract val actionCost: ActionCost

    data class MeleeAttack(override val damage: String) : DamageCausingAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SingleRangedAttack(override val damage: String, override val range: Int) : DamageCausingAction, RangedAttackAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SemiAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int, override val range: Int) : DamageCausingAction, RangedAttackAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class FullAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int, override val range: Int) : DamageCausingAction,  RangedAttackAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class WeaponReload(override val actionCost: ActionCost) : ActionOption()

    object HalfAim: EffectCausingAction, ActionOption() {
        override val actionCost =  HALF_ACTION
        override val appliesEffects = BONUS_AIM_HALF
    }

    object FullAim: EffectCausingAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
        override val appliesEffects = BONUS_AIM_FULL
    }

    interface MoveAction {
        fun getMovementRange(agilityBonus: Int): Int
        fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
    }

    object HalfMove: MoveAction, ActionOption() {
        override val actionCost: ActionCost = HALF_ACTION
        override fun getMovementRange(agilityBonus: Int): Int {
            return agilityBonus
        }
        override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
            return true  // unlike charge etc, there are no special restrictions on half move pathing
        }
    }

    data class ChargeAttack(override val damage: String): DamageCausingAction, MoveAction, ActionOption() {

        override val actionCost: ActionCost = FULL_ACTION
        override val numberOfAttacks = 1

        override fun getMovementRange(agilityBonus: Int): Int {
            return (3 * agilityBonus)
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

interface EffectCausingAction {
    val appliesEffects: Effects
}

interface RangedAttackAction {
    val range: Int
}

interface TurnAction {
    val action: ActionOption
}

class TargetedAction(override val action: ActionOption, target: UnitInstance): TurnAction {

}

class AimAction(override val action: ActionOption): TurnAction {

}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}
