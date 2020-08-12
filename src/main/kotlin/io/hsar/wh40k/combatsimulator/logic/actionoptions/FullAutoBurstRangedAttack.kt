package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.Result
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

class FullAutoBurstRangedAttack(override val damage: String, range: Int, val numberOfAttacks: Int, val ammoCost: Int = numberOfAttacks) : RangedAttack(range) {
    override val actionCost = ActionCost.HALF_ACTION
    override val hitModifier = -10

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.useAmmo(ammoCost = ammoCost)
        rollToHit(user)
                .let { rollResult ->
                    when (rollResult.result) {
                        Result.SUCCESS -> {
                            Math.min(rollResult.degreesOfResult, numberOfAttacks)
                                    .let { numHits ->
                                        applyHits(target, 1)
                                    }
                        }
                        Result.FAILURE -> return
                    }
                }
    }
}