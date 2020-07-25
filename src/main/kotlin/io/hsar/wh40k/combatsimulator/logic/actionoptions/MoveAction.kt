package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

abstract class MoveAction: ActionOption() {
    //provide some common logic for moving
    abstract fun getMaxMoveDistance(user: UnitInstance): Int

    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return true  // can always at least try and move
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return MOVING_INHERENT_VALUE
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {
        world.moveTowards(user, target, getMaxMoveDistance(user))
    }
}