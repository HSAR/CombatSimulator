package io.hsar.wh40k.combatsimulator

import com.bernardomg.tabletop.dice.history.RollHistory
import com.bernardomg.tabletop.dice.interpreter.DiceRoller
import com.bernardomg.tabletop.dice.parser.DefaultDiceParser
import com.bernardomg.tabletop.dice.parser.DiceParser
import io.hsar.wh40k.combatsimulator.Result.*
import kotlin.random.Random

enum class Result {
    SUCCESS,
    FAILURE
}

data class RollResult(val result: Result, val degreesOfResult: Short)

object Dice {

    private val parser = DefaultDiceParser()
    private val roller = DiceRoller()

    /**
     * Roll a d100 and compare against the result.
     */
    fun roll(target: Short): RollResult {
        return roll("1d100") // Gives a number between 1 and 100
                .toShort()
                .let { rolledNumber ->
                    if (rolledNumber <= target) {
                        SUCCESS
                    } else {
                        FAILURE
                    }
                            .let { result ->
                                RollResult(
                                        result = result,
                                        degreesOfResult = degreesOfResult(rolledNumber, target)
                                )
                            }
                }
    }

    fun rollInitiative(agilityBonus: Short): Short {
        return (roll("1d10") + agilityBonus) // Gives a number between 1 and 10
                .toShort()
    }

    private fun roll(diceString: String): Int = parser.parse(diceString, roller).totalRoll

    private fun degreesOfResult(a: Short, b: Short): Short = Math.abs((a / 10) - (b / 10)).toShort()
}