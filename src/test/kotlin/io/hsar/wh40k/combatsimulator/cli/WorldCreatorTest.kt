package io.hsar.wh40k.combatsimulator.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.wh40k.combatsimulator.CombatSimulation
import io.hsar.wh40k.combatsimulator.cli.input.ForcesDTO
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class WorldCreatorTest {
    @Test
    fun`Set up end to end test`() {
        // This unit test is mainly a vehicle for end to end testing

        val enemyTestFile = getResourcePath("data/test-enemy-forces-file.json")
        val enemyForcesDTO: ForcesDTO = WorldCreatorTest.objectMapper.readValue(enemyTestFile)

        val friendlyTestFile = getResourcePath("data/test-friendly-forces-file.json")
        val friendlyForcesDTO: ForcesDTO = WorldCreatorTest.objectMapper.readValue(friendlyTestFile)

        val world = WorldCreator.createWorld(listOf(friendlyForcesDTO, enemyForcesDTO));
        val sim = CombatSimulation(world);
        sim.runSimulation();
        println("Simulation finished")
        val oneSideIsWipedOut = xor(world.enemyForces.size == 0 , world.friendlyForces.size == 0)
        assertThat(oneSideIsWipedOut, equalTo(true));
    }

    private fun xor(a: Boolean, b: Boolean): Boolean {
        return (a && !b) || (!a && b)
    }

    private fun getResourcePath(resource: String): File {
        return File(this::class.java.classLoader.getResource(resource)!!.file)
    }

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}