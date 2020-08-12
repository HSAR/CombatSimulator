package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.Result.FAILURE
import io.hsar.wh40k.combatsimulator.dice.Result.SUCCESS
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat

class ChargeAttack(override val damage: String) : MeleeAttack() {
    override val actionCost = ActionCost.FULL_ACTION
    override val targetType = TargetType.ADVERSARY_TARGET
    override val hitModifier = 20
    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return world.distanceApart(user, target) <= user.getBaseStatBonus(BaseStat.AGILITY) * 3
        // TODO implement this more thoroughly
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return getHitChance(user) * getAverageDamage(target)
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        world.moveTowards(user, target, user.getBaseStatBonus(BaseStat.AGILITY) * 3)
        if(world.isInMeleeRange(user, target)) {
            rollToHit(user).let { rollResult ->
                when (rollResult.result) {
                    SUCCESS -> applyHits(target, 1)
                    FAILURE -> return
                }
            }
        }

    }

}