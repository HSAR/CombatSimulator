package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.dice.RollResult
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.Effect

abstract class MeleeAttack(appliesEffects: Collection<Effect> = emptyList()) : AttackActionOption(appliesEffects) {
    override fun rollToHit(user: UnitInstance): RollResult {
        return user.rollBaseStat(BaseStat.WEAPON_SKILL, user.getAimBonus() + hitModifier)
    }

    override fun getHitChance(user: UnitInstance): Float {
        return user.getBaseStatSuccessChance(BaseStat.WEAPON_SKILL, user.getAimBonus() + hitModifier)
    }
}