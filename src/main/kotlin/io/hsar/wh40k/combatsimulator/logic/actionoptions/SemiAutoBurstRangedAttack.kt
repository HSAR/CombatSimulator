package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.Result
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import java.lang.Math.min

class SemiAutoBurstRangedAttack(override val damage: String, range: Int, val numberOfAttacks: Int, val ammoCost: Int = numberOfAttacks) : RangedAttack(range) {
    override val actionCost = ActionCost.HALF_ACTION
    override val hitModifier = 0

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.useAmmo(ammoCost = ammoCost)
        rollToHit(user)
                .let { rollResult ->
                    when (rollResult.result) {
                        Result.SUCCESS -> {
                            min(rollResult.degreesOfResult, numberOfAttacks)
                                    .let { numHits ->
                                        applyHits(target, 1)
                                    }
                        }
                        Result.FAILURE -> return
                    }
                }
    }
}