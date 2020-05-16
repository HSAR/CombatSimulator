package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.*
import io.hsar.wh40k.combatsimulator.model.unit.*
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.Result

object AttackExecution {

    fun rollHits(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        //TODO factor in aiming etc
        val effects = attacker.currentAttributes[Attribute.EFFECTS]
        var aimBonus = 0
        when(effects) {
            is EffectValue -> {
                if(Effect.AIMED_FULL in effects.value) {
                    aimBonus = 20
                } else if (Effect.AIMED_HALF in effects.value) {
                    aimBonus = 10
                }
            }
            else -> throw RuntimeException("Effects must be of type EffectValue")
        }
        return when(action) {
            is RangedAttackAction -> attacker.rollBaseStat(BaseStat.BALLISTIC_SKILL,aimBonus)
            else -> attacker.rollBaseStat(BaseStat.WEAPON_SKILL,aimBonus)
        }
                .let { rollResult->
                    when(rollResult.result) {
                        Result.FAILURE -> 0
                        Result.SUCCESS -> when(action) {
                            is SemiAutoBurstRangedAttack -> rollResult.degreesOfResult / 2 + 1
                            is FullAutoBurstRangedAttack -> rollResult.degreesOfResult + 1
                            is SingleRangedAttack -> 1
                            is MeleeAttack -> 1
                            is ChargeAttack -> 1
                            else -> throw RuntimeException("Invalid attack type used")
                        }
                    }
                }

    }

    fun calcDamage(attacker: UnitInstance, target: UnitInstance, action: DamageCausingAction): Int {
        val damage = AverageDice.roll(action.damage)
        // for each attack, roll damage, roll bodypart and then allow for mitigation

        //now, check enemy damage mitigation for that body part
        val mitigation = when(RandomDice.randomBodyPart()) {
            BodyPart.HEAD -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_HEAD] ?: NumericValue(0)
            BodyPart.RIGHT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_R] ?: NumericValue(0)
            BodyPart.LEFT_ARM -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_ARM_L] ?: NumericValue(0)
            BodyPart.BODY -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_TORSO] ?: NumericValue(0)
            BodyPart.RIGHT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_R] ?: NumericValue(0)
            BodyPart.LEFT_LEG -> target.startingAttributes[Attribute.DAMAGE_REDUCTION_LEG_L] ?: NumericValue(0)
        }
        // TODO does damage mitigation cover toughness bonus??
        when(mitigation) {
            is NumericValue -> return damage - mitigation.value
            else -> throw TypeCastException("Invalid damage mitigation attribute used")
        }

    }

}