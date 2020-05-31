package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.wh40k.combatsimulator.CombatSimulation
import io.hsar.wh40k.combatsimulator.cli.input.ForcesDTO
import java.io.File
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class SimulateCombat : Command("simulate-combat") {

    @Parameter(names = arrayOf("--blufor", "--friendlies"), description = "Path to an input file describing friendly units", required = true)
    private var friendlyForcesFilePath = ""

    @Parameter(names = arrayOf("--opfor", "--enemies"), description = "Path to an input file describing enemy units", required = true)
    private var enemyForcesFilePath = ""

    @Parameter(names = arrayOf("--simulations", "--times"), description = "How many times to run the simulation (default: 10)", required = false)
    private var numRuns = 10

    override fun run() {
        // Read and parse input files
        listOf(friendlyForcesFilePath, enemyForcesFilePath)
                .map { forcesFilePath -> // We are enacting the same operations on both
                    // Read file and parse into object format
                    File(forcesFilePath)
                            .readText()
                            .let { forcesFileString ->
                                objectMapper.readValue<ForcesDTO>(forcesFileString)
                            }
                }
                .let { forcesList ->
                    // Run the number of simulations requested
                    (1..numRuns)
                            .map { runNumber ->
                                println("===== SIMULATION STARTING (#${runNumber.toString().padStart(3, '0')}) =====")
                                // Create World and CombatSimulation instances, then initiate combat
                                CombatSimulation(
                                        world = WorldCreator.createWorld(forcesList)
                                )
                                        .runSimulation()
                                        .also { result ->
                                            println("===== SIMULATION RESULTS (#${runNumber.toString().padStart(3, '0')}) =====")
                                            println(objectWriter.writeValueAsString(result))
                                            println("===== SIMULATION COMPLETE (#${runNumber.toString().padStart(3, '0')}) =====")
                                        }
                            }
                }
                .let { results ->
                    // Process results for summarised digest
                    results
                            .groupBy { result -> result.winner }
                            .toList()
                            .sortedByDescending { (_, roundsWonList) -> roundsWonList.count() }
                            .let { results ->
                                println("===== ALL SIMULATIONS COMPLETE ($numRuns) =====")
                                println("===== SUMMARY STARTS =====")
                                results
                                        .forEach { (winner, roundsWonList) ->
                                            val totalRoundsWon = roundsWonList.count()
                                            println("    Winner in ${(totalRoundsWon / numRuns.toDouble()).toPercentage()}% of cases: $winner")

                                            val remainingUnitsAverage = roundsWonList
                                                    .map { roundWon -> roundWon.remainingUnits.count() }
                                                    .average()
                                            println("        Average remaining units: $remainingUnitsAverage")

                                            val roundsTaken = roundsWonList
                                                    .map { roundWon -> roundWon.rounds }
                                                    .average()
                                            println("        Average rounds taken to win: $remainingUnitsAverage")
                                        }
                                println("===== SUMMARY ENDS =====")
                            }
                }
    }

    private fun List<Int>.average() = this.sum() / this.count().toDouble()

    private fun Double.toPercentage() = this * 100

    companion object {
        private val objectMapper = jacksonObjectMapper()
        private val objectWriter = objectMapper.writerWithDefaultPrettyPrinter()
    }
}

fun main(args: Array<String>) {
    val instances: Map<String, Command> = listOf(
            SimulateCombat()
    )
            .associateBy { it.name }
    val commander = JCommander()
    instances.forEach { name, command -> commander.addCommand(name, command) }

    if (args.isEmpty()) {
        commander.usage()
        System.err.println("Expected some arguments")
        exitProcess(1)
    }

    try {
        commander.parse(*args)
        val command = instances[commander.parsedCommand]
        command!!.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}
