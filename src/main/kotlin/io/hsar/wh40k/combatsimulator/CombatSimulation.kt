package io.hsar.wh40k.combatsimulator

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.AGILITY
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus

class CombatSimulation(val world: World) {

    val initiativeOrder: List<UnitInstance> = (world.friendlyForces + world.enemyForces)
            .map { eachUnit ->
                eachUnit.unit.stats.baseStats.getValue(AGILITY).getBonus()
                        .let { agilityBonus ->
                            Dice.rollInitiative(agilityBonus = agilityBonus) to eachUnit
                        }
            }
            .sortedByDescending { (initiativeRoll, _) -> initiativeRoll }
            .map { (_, eachUnit) -> eachUnit }

    fun run() {
        initiativeOrder.forEach { unit ->
            TODO("Not yet implemented")
        }
    }
}