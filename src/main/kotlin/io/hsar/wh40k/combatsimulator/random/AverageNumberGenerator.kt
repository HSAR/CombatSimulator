package io.hsar.wh40k.combatsimulator.random

import com.bernardomg.tabletop.dice.Dice
import com.bernardomg.tabletop.dice.random.NumberGenerator
import kotlin.math.roundToInt

/**
 * Fast average roll estimation.
 */
class AverageNumberGenerator : NumberGenerator {
    override fun generate(dice: Dice): Iterable<Int> {
        return generate(dice.sides)
                .let { result ->
                    (1..dice.quantity).map { result }
                }
    }

    override fun generate(max: Int): Int = (max / 2.0).roundToInt()
}