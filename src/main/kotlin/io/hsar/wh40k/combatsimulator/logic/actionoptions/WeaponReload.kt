package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue

class WeaponReload(override val actionCost: ActionCost, val setsAmmunitionTo: Int) : ActionOption() {
    override val targetType: TargetType = TargetType.SELF_TARGET

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean = true // TODO: Check if unit has spare ammo clip

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return 0.1f // Return non-zero value so that units will try to reload if possible
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        user.currentAttributes[Attribute.WEAPON_AMMUNITION] = NumericValue(setsAmmunitionTo)
    }
}