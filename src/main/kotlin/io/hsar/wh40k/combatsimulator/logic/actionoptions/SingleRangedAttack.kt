package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.Result.FAILURE
import io.hsar.wh40k.combatsimulator.dice.Result.SUCCESS
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

class SingleRangedAttack(override val damage: String, range: Int, val ammoCost: Int = 1) : RangedAttack(range) {
    override val actionCost = ActionCost.HALF_ACTION
    override val hitModifier = 10

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.useAmmo(ammoCost = ammoCost)
        rollToHit(user)
                .let { rollResult ->
                    when (rollResult.result) {
                        SUCCESS -> applyHits(target, 1)
                        FAILURE -> {
                        }
                    }
                }
    }
}