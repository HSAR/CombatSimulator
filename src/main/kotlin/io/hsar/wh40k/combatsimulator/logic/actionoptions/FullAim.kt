package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Effect

class FullAim : ActionOption() {
    override val actionCost = ActionCost.FULL_ACTION
    override val targetType = TargetType.SELF_TARGET
    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return true
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return FULL_AIMING_INHERENT_VALUE
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.setEffect(Effect.AIMED_FULL)
    }
}