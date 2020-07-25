package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.random.RollResult

abstract class RangedAttack: AttackActionOption() {
    override fun rollToHit(user: UnitInstance): RollResult {
        return user.rollBaseStat(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + bonusToHit)
    }
    override fun getHitChance(user: UnitInstance): Float {
        return user.getBaseStatSuccessChance(BaseStat.BALLISTIC_SKILL, user.getAimBonus() + bonusToHit)
    }
}