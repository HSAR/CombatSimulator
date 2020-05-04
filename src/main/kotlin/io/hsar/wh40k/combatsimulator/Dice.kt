package io.hsar.wh40k.combatsimulator

import io.hsar.wh40k.combatsimulator.Result.*
import kotlin.random.Random

enum class Result {
    SUCCESS,
    FAILURE
}

data class RollResult(val result: Result, val degreesOfResult: Short)

object Dice {

    private val random = Random.Default

    /**
     * Roll a d100 and compare against the result.
     */
    fun roll(target: Short): RollResult {
        return ((random.nextDouble() * 100) + 1) // Gives a number between 1 and 100
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
        return (((random.nextDouble() * 10) + 1) + agilityBonus).toShort() // Gives a number between 1 and 10
    }


    private fun degreesOfResult(a: Short, b: Short): Short = Math.abs((a / 10) - (b / 10)).toShort()
}