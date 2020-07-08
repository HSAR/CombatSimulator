package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.RangedAttackAction
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.BODY
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.HEAD
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.LEFT_ARM
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.LEFT_LEG
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.RIGHT_ARM
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart.RIGHT_LEG
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.random.AverageDice
import io.hsar.wh40k.combatsimulator.random.RandomDice


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


    }



}