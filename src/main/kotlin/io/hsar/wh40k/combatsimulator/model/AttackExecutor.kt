package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.RangedAttackAction
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_HEAD
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_TORSO
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.EFFECTS
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.BODY
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.HEAD
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.LEFT_ARM
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.LEFT_LEG
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.RIGHT_ARM
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.RIGHT_LEG
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats.DODGE
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats.PARRY
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.Result
import java.lang.Integer.max


class AttackExecutor {

    fun rollHits(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        val effects = attacker.currentAttributes[EFFECTS] ?: EffectValue(listOf<Effect>())
        return when (effects) {
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
                    when (action) {
                        is RangedAttackAction -> attacker.rollBaseStat(BaseStat.BALLISTIC_SKILL, aimBonus)
                        else -> attacker.rollBaseStat(BaseStat.WEAPON_SKILL, aimBonus)
                    }
                            .let { rollResult ->
                                action.determineHitCount(rollResult)
                            }
                }

    }

    fun calcDamage(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        // Target attempts to dodge/parry
        // TODO: Only allow one dodge/parry per turn
        // TODO: Allow parrying only if melee
        return target.unit.stats.derivedStats
                .let { derivedStats ->
                    max(
                            derivedStats[DODGE] ?: -20,
                            derivedStats[PARRY] ?: -20
                    ) // Default modifier is -20 if not trained
                }
                .let { saveThreshold ->
                    when (RandomDice.roll(saveThreshold).result) {
                        Result.SUCCESS -> 0 // no damage taken, hit was avoided
                        Result.FAILURE -> {
                            // roll damage, roll body part and then allow for mitigation
                            val damage = AverageDice.roll(action.damage)

                            //now, check enemy damage mitigation for that body part
                            val mitigation = when (randomBodyPart()) {
                                HEAD -> target.currentAttributes[DAMAGE_REDUCTION_HEAD] ?: NumericValue(0)
                                RIGHT_ARM -> target.currentAttributes[DAMAGE_REDUCTION_ARM_R] ?: NumericValue(0)
                                LEFT_ARM -> target.currentAttributes[DAMAGE_REDUCTION_ARM_L] ?: NumericValue(0)
                                BODY -> target.currentAttributes[DAMAGE_REDUCTION_TORSO] ?: NumericValue(0)
                                RIGHT_LEG -> target.currentAttributes[DAMAGE_REDUCTION_LEG_R] ?: NumericValue(0)
                                LEFT_LEG -> target.currentAttributes[DAMAGE_REDUCTION_LEG_L] ?: NumericValue(0)
                            }
                                    .let { mitigationVal ->
                                        when (mitigationVal) {
                                            is NumericValue -> mitigationVal.value
                                            else -> throw TypeCastException("Invalid damage mitigation attribute used")
                                        }
                                    }

                            damage - mitigation
                        }
                    }


                }
    }

    fun randomBodyPart(): BodyPart {
        RandomDice.roll("1d100")
                .let { diceRoll ->
                    return when (diceRoll) {
                        in 0..10 -> HEAD
                        in 11..20 -> RIGHT_ARM
                        in 21..30 -> LEFT_ARM
                        in 31..70 -> BODY
                        in 71..85 -> RIGHT_LEG
                        in 86..100 -> LEFT_LEG
                        else -> throw RuntimeException("d100 roll outside of 1-100")
                    }
                }
    }

}