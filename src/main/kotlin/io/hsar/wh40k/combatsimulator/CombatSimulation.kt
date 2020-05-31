package io.hsar.wh40k.combatsimulator

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.AGILITY
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus
import io.hsar.wh40k.combatsimulator.random.RandomDice.rollInitiative
import kotlin.Exception

class CombatSimulation(val world: World) {

    private val initiativeOrder: MutableList<UnitInstance> = (world.friendlyForces + world.enemyForces)
            .map { eachUnit ->
                eachUnit.unit.stats.baseStats.getValue(AGILITY).getBonus()
                        .let { agilityBonus ->
                            rollInitiative(agilityBonus = agilityBonus) to eachUnit
                        }
            }
            .sortedByDescending { (initiativeRoll, _) -> initiativeRoll }
            .map { (_, eachUnit) -> eachUnit }
            .toMutableList()

    fun runRound() {
        initiativeOrder.forEach { unit ->
            if((unit.currentAttributes.getValue(Attribute.CURRENT_HEALTH) as NumericValue).value > 0) {
                // check if unit alive rather than try removing dead units from list as concurrency risks
                // caller needs to exception handle this in case CURRENT_HEALTH not provided in json
                unit.tacticalActionStrategy
                        .decideTurnActions(world, unit, unit.availableActionOptions)
                        .let { actionsToExecute ->
                            unit.executeActions(actionsToExecute)
                        }
                        .also {
                            val deadUnits = world.findDead()
                            deadUnits.forEach { deadUnit->
                                println("${deadUnit.name} died")
                            }
                            world.removeUnits(deadUnits)
                        }
            }

        }
    }

    fun runSimulation() {
        var roundNum = 1
        while(world.enemyForces.size > 0 && world.friendlyForces.size > 0) {
            printRoundStartStatus(roundNum)
            try {
                runRound()
            } catch(e: Exception) {
                println("Error: ${e.message}")
            }

            roundNum++
        }
        if(world.enemyForces.size == 0) {
            println("Party wins")
        } else {
            println("Enemies win")
        }
    }

    private fun printRoundStartStatus(roundNum: Int) {
        println("Start of combat round $roundNum")
        println("Friendly units alive: ${world.friendlyForces.map{it.name}}")
        println("Enemy units alive: ${world.enemyForces.map{it.name}}")
    }

}