package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.RangedAttackAction
import io.hsar.wh40k.combatsimulator.model.unit.*
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice
import java.lang.IllegalStateException


class AttackExecutor {

    fun rollHits(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        val effects = attacker.currentAttributes[Attribute.EFFECTS] ?: EffectValue(listOf<Effect>())
        return when(effects) {
            is EffectValue -> {
                when {
                    Effect.AIMED_FULL in effects.value -> 20
                    Effect.AIMED_HALF in effects.value -> 10
                    else -> 0
                }
            }
            else -> throw IllegalStateException("Effects must be of type EffectValue")
        }
                .let { aimBonus ->
                    when(action) {
                        is RangedAttackAction -> attacker.rollBaseStat(BaseStat.BALLISTIC_SKILL,aimBonus)
                        else -> attacker.rollBaseStat(BaseStat.WEAPON_SKILL,aimBonus)
                    }
                            .let { rollResult->
                                action.determineHitCount(rollResult)
                            }
                }

    }

    fun calcDamage(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        val damage = AverageDice.roll(action.damage)
        // for each attack, roll damage, roll body part and then allow for mitigation

        //now, check enemy damage mitigation for that body part
        val mitigation = when(randomBodyPart()) {
            BodyPart.HEAD -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_HEAD] ?: NumericValue(0)
            BodyPart.RIGHT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_R] ?: NumericValue(0)
            BodyPart.LEFT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_L] ?: NumericValue(0)
            BodyPart.BODY -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_TORSO] ?: NumericValue(0)
            BodyPart.RIGHT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_R] ?: NumericValue(0)
            BodyPart.LEFT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_L] ?: NumericValue(0)
        }

        when(mitigation) {
            is NumericValue -> return damage - mitigation.value
            else -> throw TypeCastException("Invalid damage mitigation attribute used")
        }

    }

    fun randomBodyPart(): BodyPart {
        RandomDice.roll("1d100")
                .let { diceRoll ->
                    return when(diceRoll) {
                        in 0..10 -> BodyPart.HEAD
                        in 11..20 -> BodyPart.RIGHT_ARM
                        in 21..30 -> BodyPart.LEFT_ARM
                        in 31..70 -> BodyPart.BODY
                        in 71..85 -> BodyPart.RIGHT_LEG
                        in 86..100 -> BodyPart.LEFT_LEG
                        else -> throw RuntimeException("d100 roll outside of 1-100")
                    }
                }
    }

}