package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus

object TacticalActionStrategy {
    fun decideTurnActions(world: World, thisUnit: UnitInstance): List<TurnAction> {
        TODO("Not yet implemented")
    }

    fun canHalfMoveToEnemy(world: World, thisUnit: UnitInstance, enemy: UnitInstance): Boolean {
        return  world.distanceApart(thisUnit, enemy)  - 1 <= // -1 as only need to be next to them, not on same square
                thisUnit.unit.stats.baseStats.getValue(BaseStat.AGILITY).getBonus()
    }

    fun canChargeToEnemy(world: World, thisUnit: UnitInstance, enemy: UnitInstance): Boolean {
        //TODO this currently doesn't worry about the straight line clause or minimum distance
        return  world.distanceApart(thisUnit, enemy)  - 1 <=
                3 * (thisUnit.unit.stats.baseStats.getValue(BaseStat.AGILITY).getBonus())
    }

    //TODO add collision detection
}