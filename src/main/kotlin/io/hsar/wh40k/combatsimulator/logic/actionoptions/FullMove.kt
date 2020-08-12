package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat

class FullMove : MoveAction() {
    override val actionCost = ActionCost.FULL_ACTION
    override val targetType = TargetType.ANY_TARGET  // can move towards any character

    override fun getMaxMoveDistance(user: UnitInstance): Int {
        return user.getBaseStatBonus(BaseStat.AGILITY) * 2
    }
}