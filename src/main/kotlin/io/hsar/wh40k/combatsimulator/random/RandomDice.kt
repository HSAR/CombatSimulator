package io.hsar.wh40k.combatsimulator.random

import com.bernardomg.tabletop.dice.interpreter.DiceRoller
import com.bernardomg.tabletop.dice.parser.DefaultDiceParser
import io.hsar.wh40k.combatsimulator.model.BodyPart
import io.hsar.wh40k.combatsimulator.random.Result.*

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

    fun randomBodyPart(): BodyPart {
        roll("1d100")
        .let { diceRoll ->
               when(diceRoll) {
                   in 0..10 -> return BodyPart.HEAD
                   in 11..20 -> return BodyPart.RIGHT_ARM
                   in 21..30 -> return BodyPart.LEFT_ARM
                   in 31..70 -> return BodyPart.BODY
                   in 71..85 -> return BodyPart.RIGHT_LEG
                   in 86..100 -> return BodyPart.LEFT_LEG
                   else -> throw RuntimeException("d100 roll outside of 1-100")
               }
                }
    }

    private fun degreesOfResult(a: Int, b: Int): Int = Math.abs((a / 10) - (b / 10))
}