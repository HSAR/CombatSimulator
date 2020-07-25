package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.RollResult

/*
    Provide some concrete implementations of common damage calculation tasks that can be re-used by child classes
*/
abstract class AttackActionOption: ActionOption() {

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