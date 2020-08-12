package io.hsar.wh40k.combatsimulator.dice

import com.bernardomg.tabletop.dice.interpreter.DiceRoller
import com.bernardomg.tabletop.dice.parser.DefaultDiceParser
import io.hsar.wh40k.combatsimulator.dice.Result.FAILURE
import io.hsar.wh40k.combatsimulator.dice.Result.SUCCESS

enum class Result {
    SUCCESS,
    FAILURE
}

data class RollResult(val result: Result, val degreesOfResult: Int)

object RandomDice {

    private val parser = DefaultDiceParser()
    private val roller = DiceRoller()

    /**
     * Roll a d100 and compare against the result.
     */
    fun roll(target: Int): RollResult {
        return roll("1d100")
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

    fun rollInitiative(agilityBonus: Int): Int = (roll("1d10") + agilityBonus)

    /**
     * Roll an arbitrary dice string and receive the result.
     */
    fun roll(diceString: String): Int = parser.parse(diceString, roller).totalRoll

    private fun degreesOfResult(a: Int, b: Int): Int = Math.abs((a / 10) - (b / 10))
}