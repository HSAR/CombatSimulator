package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class SimulateCombat: Command("simulate-combat") {

    @Parameter(names = arrayOf("--blufor", "--friendlies"), description = "Path to an input file describing friendly units", required = true)
    private var friendlyForcesFilePath = ""

    @Parameter(names = arrayOf("--opfor", "--enemies"), description = "Path to an input file describing enemy units", required = true)
    private var enemyForcesFilePath = ""

    override fun run() {
        TODO("Not yet implemented")
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
