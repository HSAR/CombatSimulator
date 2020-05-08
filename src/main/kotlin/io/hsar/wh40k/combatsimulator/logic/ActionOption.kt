package io.hsar.wh40k.combatsimulator.logic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.Effect.*

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "actionType",
        visible = true)
sealed class ActionOption {
    abstract val actionCost: ActionCost
}

data class MeleeAttack(override val damage: String, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val numberOfAttacks = 1
}

data class SingleRangedAttack(override val range: Int, override val damage: String, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val numberOfAttacks = 1
}

data class SemiAutoBurstRangedAttack(override val range: Int, override val damage: String, override val numberOfAttacks: Int, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
}

data class FullAutoBurstRangedAttack(override val range: Int, override val damage: String, override val numberOfAttacks: Int, override val appliesEffects: List<Effect> = emptyList()) : DamageCausingAction, RangedAttackAction, EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
}

data class WeaponReload(override val actionCost: ActionCost) : ActionOption()

object HalfAim: EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val appliesEffects = listOf(AIMED_HALF)
}

object FullAim: EffectCausingAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val appliesEffects = listOf(AIMED_FULL)
}

interface MoveAction {
    fun getMovementRange(agilityBonus: Int): Int
    fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean
}

object HalfMove: MoveAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override fun getMovementRange(agilityBonus: Int): Int {
        return agilityBonus
    }
    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return true  // unlike charge etc, there are no special restrictions on half move pathing
    }
}

data class ChargeAttack(override val damage: String): DamageCausingAction, EffectCausingAction, MoveAction, ActionOption() {
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

interface DamageCausingAction {
    val damage: String
    val numberOfAttacks: Int
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
