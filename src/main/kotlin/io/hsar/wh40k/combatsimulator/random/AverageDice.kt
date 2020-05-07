package io.hsar.wh40k.combatsimulator.random

import com.bernardomg.tabletop.dice.interpreter.DiceRoller
import com.bernardomg.tabletop.dice.parser.DefaultDiceParser

object AverageDice {

    private val parser = DefaultDiceParser()
    private val roller = DiceRoller(AverageNumberGenerator())

    fun roll(diceString: String): Int = parser.parse(diceString, roller).totalRoll
}