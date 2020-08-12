package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.RollResult
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_AMMUNITION
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue

abstract class RangedAttack(val range: Int, appliesEffects: Collection<Effect> = emptyList()) : AttackActionOption(appliesEffects) {
    override fun rollToHit(user: UnitInstance): RollResult {
        return user.rollBaseStat(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + hitModifier)
    }

    override fun getHitChance(user: UnitInstance): Float {
        return user.getBaseStatSuccessChance(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + hitModifier)
    }

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        // check target is in melee range of user
        return world.distanceApart(user, target) <= range
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return getHitChance(user) * getAverageDamage(target)
    }

    protected fun UnitInstance.useAmmo(ammoCost: Int) {
        this.currentAttributes[WEAPON_AMMUNITION].let { currentAmmo ->
            this.currentAttributes[WEAPON_AMMUNITION] = (currentAmmo as NumericValue) + NumericValue(-ammoCost)
        }
    }
}