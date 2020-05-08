package io.hsar.wh40k.combatsimulator.logic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.Effect.*

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "actionType",
        visible = true)
sealed class TurnAction {
    abstract val actionCost: ActionCost

    data class MeleeAttack(override val damage: String, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SingleRangedAttack(override val damage: String, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class SemiAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
    }

    data class FullAutoBurstRangedAttack(override val damage: String, override val numberOfAttacks: Int, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
    }

    data class WeaponReload(override val actionCost: ActionCost) : TurnAction()

    object HalfAim: EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
        override val appliesEffects = listOf(AIMED_HALF)
    }

    object FullAim: EffectCausingAction, TurnAction() {
        override val actionCost = HALF_ACTION
        override val appliesEffects = listOf(AIMED_FULL)
    }

    abstract class MoveAction: TurnAction() {
        abstract fun getMovementRange(agilityBonus: Int): Int
        abstract fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
    }

    object HalfMove: MoveAction() {
        override val actionCost = HALF_ACTION
        override fun getMovementRange(agilityBonus: Int): Int {
            return agilityBonus
        }
        override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
            return true  // unlike charge etc, there are no special restrictions on half move pathing
        }
    }

    data class ChargeAttack(override val damage: String): DamageCausingAction, EffectCausingAction, MoveAction() {
        override val actionCost = FULL_ACTION
        override val appliesEffects = listOf(CHARGING)
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
    val appliesEffects: List<Effect>
}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}