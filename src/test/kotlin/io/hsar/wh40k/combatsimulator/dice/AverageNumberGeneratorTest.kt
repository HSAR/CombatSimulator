package io.hsar.wh40k.combatsimulator.dice

import com.bernardomg.tabletop.dice.DefaultDice
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.jupiter.api.Test

class AverageNumberGeneratorTest {

    @Test
    fun `generating with Dice is strictly deterministic`() {
        // Arrange
        val testMax = 20 // roll a twenty-sided dice
        val testDice = DefaultDice(3, testMax) // roll three of the twenty-sided dice
        val objectUnderTest = AverageNumberGenerator()

        // Act
        val results = (1..10).map { // execute ten times
            objectUnderTest.generate(testDice)
        }
                .flatten()

        // Assert
        results.forEach { result ->
            assertThat("Each result should be above minimum value", result, greaterThanOrEqualTo(EXPECTED_MINIMUM_VALUE))
            assertThat("Each result should be below maximum value", result, lessThanOrEqualTo(testMax))
            assertThat("Each result should be the same", result, equalTo(results.first()))
        }
    }

    @Test
    fun `generating by Int maximum is strictly deterministic`() {
        // Arrange
        val testMax = 20
        val objectUnderTest = AverageNumberGenerator()

        // Act
        val results = (1..10).map {
            objectUnderTest.generate(testMax) // roll a twenty-sided dice
        }

        // Assert
        results.forEach { result ->
            assertThat("Each result should be above minimum value", result, greaterThanOrEqualTo(EXPECTED_MINIMUM_VALUE))
            assertThat("Each result should be below maximum value", result, lessThanOrEqualTo(testMax))
            assertThat("Each result should be the same", result, equalTo(results.first()))
        }
    }

    companion object {
        private const val EXPECTED_MINIMUM_VALUE = 1
    }
}