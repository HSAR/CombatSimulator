package io.hsar.wh40k.combatsimulator.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.AGILITY
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.BALLISTIC_SKILL
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.FELLOWSHIP
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.INTELLIGENCE
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.MAX_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.STRENGTH
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.TOUGHNESS
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.WEAPON_SKILL
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat.WILLPOWER
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats.DODGE
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats.PARRY
import io.hsar.wh40k.combatsimulator.model.unit.Stats
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.io.File

class InputParseTest {

    @Test
    fun `correctly parses a test forces file`() {
        // Arrange
        val testFile = getResourcePath("data/test-forces-file.json")

        val expected = ForcesInput(
                units = listOf(
                        UnitDTO(
                                unitRef = "banditRanged",
                                description = "They give the players some adds to worry about. This one shoots them.",
                                stats = Stats(
                                        baseStats = mapOf(
                                                MAX_HEALTH to 8,
                                                WEAPON_SKILL to 30,
                                                BALLISTIC_SKILL to 20,
                                                STRENGTH to 20,
                                                TOUGHNESS to 20,
                                                AGILITY to 20,
                                                INTELLIGENCE to 20,
                                                WILLPOWER to 20,
                                                FELLOWSHIP to 20
                                        ),
                                        derivedStats = mapOf(
                                                DODGE to 0,
                                                PARRY to -20
                                        )
                                ),
                                equipmentRefs = listOf(
                                        "wpn_pistol_stubRevolver",
                                        "amr_basic_heavyLeather"
                                )
                        ),
                        UnitDTO(
                                unitRef = "banditMelee",
                                description = "They give the players some adds to worry about. This one runs up and hits them.",
                                stats = Stats(
                                        baseStats = mapOf(
                                                MAX_HEALTH to 8,
                                                WEAPON_SKILL to 20,
                                                BALLISTIC_SKILL to 30,
                                                STRENGTH to 20,
                                                TOUGHNESS to 20,
                                                AGILITY to 30,
                                                INTELLIGENCE to 20,
                                                WILLPOWER to 20,
                                                FELLOWSHIP to 20
                                        ),
                                        derivedStats = mapOf(
                                                DODGE to -20,
                                                PARRY to 0
                                        )
                                ),
                                equipmentRefs = listOf(
                                        "wpn_melee_staff",
                                        "amr_basic_heavyLeather"
                                )
                        )
                ),
                unitsToSpawn = mapOf(
                        "banditRanged" to listOf("banditRanged1", "banditRanged2", "banditRanged3", "banditRanged4"),
                        "banditMelee" to listOf("banditMelee1", "banditMelee2")
                )
        )

        // Act
        val result: ForcesInput = objectMapper.readValue(testFile)

        // Assert
        assertThat(result, equalTo(expected))
    }

    private fun getResourcePath(resource: String): File {
        return File(this::class.java.classLoader.getResource(resource)!!.file)
    }

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}