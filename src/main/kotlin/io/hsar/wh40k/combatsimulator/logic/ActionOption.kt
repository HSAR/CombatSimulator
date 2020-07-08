package io.hsar.wh40k.combatsimulator.logic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hsar.wh40k.combatsimulator.logic.ActionCost.FULL_ACTION
import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.*
import io.hsar.wh40k.combatsimulator.model.unit.Effect.AIMED_FULL
import io.hsar.wh40k.combatsimulator.model.unit.Effect.AIMED_HALF
import io.hsar.wh40k.combatsimulator.model.unit.Effect.CHARGING
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.Result
import io.hsar.wh40k.combatsimulator.random.RollResult

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "actionType",
        visible = true)
@JsonIgnoreProperties(value = ["actionType"])
abstract class ActionOption {
    abstract val actionCost: ActionCost
    abstract val targetType: TargetType
    abstract fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean
    abstract fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float
    abstract fun apply(world: World, user: UnitInstance, target: UnitInstance): Unit

    companion object {
        // put all the non-dynamic expected action values in one place for easy balancing
        val HALF_AIMING_INHERENT_VALUE = 0.5f
        val FULL_AIMING_INHERENT_VALUE = 0.8f
        val MOVING_INHERENT_VALUE = 1.0f
    }
}

abstract class AttackActionOption: ActionOption() {
    /*
    Provide some concrete implementations of common damage calculation tasks that can be re-used by child classes
     */
    abstract val damage: String
    abstract val bonusToHit: Int

    abstract fun rollToHit(user: UnitInstance): RollResult
    abstract fun getHitChance(user: UnitInstance): Float

    override val targetType = TargetType.ADVERSARY_TARGET

    fun applyHits(target: UnitInstance, numHits: Int) {
        repeat(numHits) {
            target.receiveDamage(rollDamage(target))
        }
    }

    private fun rollDamage(target: UnitInstance): Int {
        val damage = RandomDice.roll(damage)
        return maxOf(damage - calcMitigation(target), 0)
    }

    fun getAverageDamage(target: UnitInstance): Int {
        val damage = AverageDice.roll(damage)
        // for each attack, roll damage, roll body part and then allow for mitigation

        //now, check enemy damage mitigation for that body part
        val mitigation = calcMitigation(target)
        return maxOf(damage - mitigation, 0)
    }

    private fun calcMitigation(target: UnitInstance): Int {
        val mitigation = when (UnitInstance.randomBodyPart()) {
            BodyPart.HEAD -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_HEAD] ?: NumericValue(0)
            BodyPart.RIGHT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_R] ?: NumericValue(0)
            BodyPart.LEFT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_L] ?: NumericValue(0)
            BodyPart.BODY -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_TORSO] ?: NumericValue(0)
            BodyPart.RIGHT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_R] ?: NumericValue(0)
            BodyPart.LEFT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_L] ?: NumericValue(0)
        }
        return when(mitigation) {
            is NumericValue ->mitigation.value  // can't deal negative damage
            else -> throw TypeCastException("Invalid damage mitigation attribute used")
        }
    }
}

abstract class MeleeAttack: AttackActionOption() {
    override fun rollToHit(user: UnitInstance): RollResult {
        return user.rollBaseStat(BaseStat.WEAPON_SKILL, user.getAimBonus() + bonusToHit)
    }

    override fun getHitChance(user: UnitInstance): Float {
        return user.getBaseStatSuccessChance(BaseStat.WEAPON_SKILL, user.getAimBonus() + bonusToHit)
    }
}

abstract class RangedAttack: AttackActionOption() {
    override fun rollToHit(user: UnitInstance): RollResult {
        return user.rollBaseStat(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + bonusToHit)
    }
    override fun getHitChance(user: UnitInstance): Float {
        return user.getBaseStatSuccessChance(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + bonusToHit)
    }
}

class StandardMeleeAttack(override val damage: String):  MeleeAttack() {

    override val actionCost = HALF_ACTION
    override val bonusToHit = 10

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        // check target is in melee range of user
        return world.isInMeleeRange(user, target)
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return getHitChance(user) * getAverageDamage(target)
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance): Unit {
        rollToHit(user).let { rollResult ->
            when(rollResult.result) {
                Result.SUCCESS -> applyHits(target, 1)
                Result.FAILURE -> return
            }
        }

    }
}

class SingleRangedAttack(override val damage: String, val range: Int): RangedAttack() {
    override val actionCost = HALF_ACTION
    override val bonusToHit = 10

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        // check target is in melee range of user
        return world.distanceApart(user, target) <= range
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return getHitChance(user) * getAverageDamage(target)
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance): Unit {
        rollToHit(user).let { rollResult ->
            when(rollResult.result) {
                Result.SUCCESS -> applyHits(target, 1)
                Result.FAILURE -> return
            }
        }
    }
}

class HalfAim : ActionOption() {
    override val actionCost = HALF_ACTION
    override val targetType = TargetType.SELF_TARGET
    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return true
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return ActionOption.HALF_AIMING_INHERENT_VALUE
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.setEffect(AIMED_HALF)
    }
}

class FullAim : ActionOption() {
    override val actionCost = FULL_ACTION
    override val targetType = TargetType.SELF_TARGET
    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return true
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return ActionOption.FULL_AIMING_INHERENT_VALUE
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.setEffect(AIMED_FULL)
    }
}

abstract class MoveAction: ActionOption() {
    //provide some common logic for moving
    abstract val maxDistance: Int

    abstract fun getMaxMoveDistance(user: UnitInstance): Int

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        TODO("Not yet implemented")
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return ActionOption.MOVING_INHERENT_VALUE
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        world.moveTowards(user, target, getMaxMoveDistance(user))
    }
}

object HalfMove : MoveAction, ActionOption() {
    override val actionCost = HALF_ACTION
    override val targetType = TargetType.ANY_TARGET  // can move towards any character
    override fun getMovementRange(agilityBonus: Int): Int {
        return agilityBonus
    }

    override fun isValidMovementPath(startPoint: MapPosition, endPoint: MapPosition): Boolean {
        return true  // unlike charge etc, there are no special restrictions on half move pathing
    }
}

data class ChargeAttack(override val damage: String) : DamageCausingAction, EffectCausingAction, MoveAction, ActionOption() {
    override val actionCost = FULL_ACTION
    override val targetType = TargetType.ADVERSARY_TARGET
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

class TargetedAction(val action: ActionOption, val target: UnitInstance){}

/**
 * The enemy targeted may cause an effect to be applied, i.e. long range
 */
/*class TargetedAction(override val action: ActionOption, val target: UnitInstance, effectsToApply: List<Effect> = emptyList()) : EffectCausingAction, TurnAction {
    override val appliesEffects: List<Effect> = effectsToApply + ((action as? EffectCausingAction)?.appliesEffects
            ?: emptyList())
}*/


//TODO maybe nest these enums inside ActionOption to avoid namespace pollution
enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}

enum class TargetType {
    SELF_TARGET,
    ADVERSARY_TARGET,
    ALLY_TARGET,
    ANY_TARGET
}
