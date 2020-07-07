package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.dice.AverageDice

object ExpectedDamageCalculator {

    fun calculate(targetedAction: TargetedAction): Double {
        return when(val action = targetedAction.action) {
            is DamageCausingAction -> action.damage
                    .let { damageString ->
                        AverageDice.roll(damageString).toDouble()
                    }
            else -> 0.0
        }
    }
}