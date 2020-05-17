package io.hsar.wh40k.combatsimulator.logic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.Effect.AIMED_FULL
import io.hsar.wh40k.combatsimulator.model.unit.Effect.AIMED_HALF
import io.hsar.wh40k.combatsimulator.model.unit.Effect.CHARGING
import io.hsar.wh40k.combatsimulator.random.Result
import io.hsar.wh40k.combatsimulator.random.RollResult

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "actionType",
        visible = true)
@JsonIgnoreProperties(value = ["actionType"])
sealed class ActionOption {
    abstract val actionCost: ActionCost
}

data class MeleeAttack(
        override val damage: String,
        override val appliesEffects: List<Effect> = emptyList()
) : DamageCausingAction, EffectCausingAction, ActionOption() {

    override val actionCost = HALF_ACTION
    override val numberOfAttacks = 1
    override fun determineHitCount(rollResult: RollResult): Int {
        return when(rollResult.result) {
            Result.SUCCESS -> 1
            Result.FAILURE -> 0
        }
    }
}

data class SingleRangedAttack(
        override val range: Int,
        override val damage: String,
        override val appliesEffects: List<Effect> = emptyList()
) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val numberOfAttacks = 1
    override fun determineHitCount(rollResult: RollResult): Int {
        return when(rollResult.result) {
            Result.SUCCESS -> 1
            Result.FAILURE -> 0
        }
    }
}

data class SemiAutoBurstRangedAttack(
        override val range: Int,
        override val damage: String,
        override val numberOfAttacks: Int,
        override val appliesEffects: List<Effect> = emptyList()
) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override fun determineHitCount(rollResult: RollResult): Int {
        return when(rollResult.result) {
            Result.SUCCESS -> 1 + rollResult.degreesOfResult / 2
            Result.FAILURE -> 0
        }
    }
}

data class FullAutoBurstRangedAttack(
        override val range: Int,
        override val damage: String,
        override val numberOfAttacks: Int,
        override val appliesEffects: List<Effect> = emptyList()
) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override fun determineHitCount(rollResult: RollResult): Int {
        return when(rollResult.result) {
            Result.SUCCESS -> 1 + rollResult.degreesOfResult
            Result.FAILURE -> 0
        }
    }
}

data class WeaponReload(override val actionCost: ActionCost, val setsAmmunitionTo: Int) : ActionOption()

object HalfAim : EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val appliesEffects = listOf(AIMED_HALF)
}

object FullAim : EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val appliesEffects = listOf(AIMED_FULL)
}

interface MoveAction {
    fun getMovementRange(agilityBonus: Int): Int
    fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
}

object HalfMove : MoveAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override fun getMovementRange(agilityBonus: Int): Int {
        return agilityBonus
    }

    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return true  // unlike charge etc, there are no special restrictions on half move pathing
    }
}

data class ChargeAttack(override val damage: String) : DamageCausingAction, EffectCausingAction, MoveAction, ActionOption() {
    override val actionCost = FULL_ACTION
    override val appliesEffects = listOf(CHARGING)
    override val numberOfAttacks = 1

    override fun determineHitCount(rollResult: RollResult): Int {
        return when(rollResult.result) {
            Result.SUCCESS -> 1
            Result.FAILURE -> 0
        }
    }

    override fun getMovementRange(agilityBonus: Int): Int {
        return (3 * agilityBonus)
    }

    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return startPoint - endPoint >= 4
    }
}

interface DamageCausingAction {
    val damage: String
    val numberOfAttacks: Int
    fun determineHitCount(rollResult: RollResult) : Int
}

interface EffectCausingAction {
    val appliesEffects: List<Effect>
}

interface RangedAttackAction {
    val range: Int
}

interface TurnAction {
    val action: ActionOption
}

/**
 * The enemy targeted may cause an effect to be applied, i.e. long range
 */
class TargetedAction(override val action: ActionOption, val target: UnitInstance, effectsToApply: List<Effect> = emptyList()) : EffectCausingAction, TurnAction {
    override val appliesEffects: List<Effect> = effectsToApply + ((action as? EffectCausingAction)?.appliesEffects
            ?: emptyList())
}

class AimAction(override val action: ActionOption) : TurnAction {

}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}
