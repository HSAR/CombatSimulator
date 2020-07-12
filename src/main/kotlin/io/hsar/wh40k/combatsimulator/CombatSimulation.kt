package io.hsar.wh40k.combatsimulator

import io.hsar.wh40k.combatsimulator.Winner.ENEMY
import io.hsar.wh40k.combatsimulator.Winner.FRIENDLY
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.AGILITY
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.StatUtils.getBonus
import io.hsar.wh40k.combatsimulator.random.RandomDice.rollInitiative
import kotlin.contracts.contract

class CombatSimulation(val world: World) {

    private val initiativeOrder: List<UnitInstance> = (world.friendlyForces + world.enemyForces)
            .map { eachUnit ->
                eachUnit.unit.stats.baseStats.getValue(AGILITY).getBonus()
                        .let { agilityBonus ->
                            rollInitiative(agilityBonus = agilityBonus) to eachUnit
                        }
            }
            .sortedByDescending { (initiativeRoll, _) -> initiativeRoll }
            .map { (_, eachUnit) -> eachUnit }

    fun runRound() {
        initiativeOrder.forEach { unit ->
            if ((unit.currentAttributes.getValue(Attribute.CURRENT_HEALTH) as NumericValue).value > 0) {
                // check if unit alive rather than try removing dead units from list as concurrency risks
                // caller needs to exception handle this in case CURRENT_HEALTH not provided in json
                unit.tacticalActionStrategy
                        .decideTurnActions(world, unit, unit.availableActionOptions)
                        .forEach { targetedAction ->
                            if(targetedAction.action.isLegal(world, unit, targetedAction.target)) {
                                // need to check iteratively rather than a filter as previous actions in the
                                // combo may have affected whether this is now legal
                                targetedAction.action.apply(world, unit, targetedAction.target)
                            }

                        }
                        .also {
                            world.findDead().let { deadUnits ->
                                if (deadUnits.any()) {
                                    println("    Deaths: ${deadUnits.map { it.name }}")
                                    world.removeUnits(deadUnits)
                                }
                            }
                        }
            }

        }
    }

    fun runSimulation(): SimulationResult {
        (1..Int.MAX_VALUE)
                .forEach { roundNum ->
                    printRoundStartStatus(roundNum)
                    runRound()
                    if (world.enemyForces.size == 0 || world.friendlyForces.size == 0) {
                        val (winner, remainingForces) = when {
                            world.enemyForces.size == 0 -> FRIENDLY to world.friendlyForces
                            world.friendlyForces.size == 0 -> ENEMY to world.enemyForces
                            else -> throw IllegalStateException("Unknown winner")
                        }

                        return SimulationResult(
                                winner = winner,
                                rounds = roundNum,
                                remainingUnits = remainingForces.map { eachUnit -> UnitSummary(eachUnit) }
                        )
                    }
                }

        throw IllegalStateException("Combat didn't end after ${Int.MAX_VALUE} rounds. Some kind of stalemate has likely occurred.")
    }

    private fun printRoundStartStatus(roundNum: Int) {
        println("Start of combat round $roundNum")
        println("    FRIENDLY units alive: ${world.friendlyForces.map { it.name }}")
        println("    ENEMY units alive: ${world.enemyForces.map { it.name }}")
    }

}