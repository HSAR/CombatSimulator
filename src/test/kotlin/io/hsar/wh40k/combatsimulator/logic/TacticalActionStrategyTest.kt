package io.hsar.wh40k.combatsimulator.logic

import TestUtils
import com.nhaarman.mockito_kotlin.any
import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class TacticalActionStrategyTest {
    private val meleeAttack = StandardMeleeAttack("1d10+4")
    private val singleRangedAttack = SingleRangedAttack(damage = "1d10+2", range = 30)
    private val chargeAttack = ChargeAttack("1d10+5")
    private val aimAction = HalfAim()


    @Test
    fun `decideTurnActions returns legal combo with highest EV`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val possibleActions = TestUtils.getBasicActions()

        val tas = TacticalActionStrategy
        val halfAimMock = mock(HalfAim::class.java)
        `when`(halfAimMock.isLegal(any(), any(), any()))
                .thenReturn(false)

        val x = 1


    }
}
