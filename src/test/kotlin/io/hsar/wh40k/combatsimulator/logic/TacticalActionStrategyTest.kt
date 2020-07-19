package io.hsar.wh40k.combatsimulator.logic

import TestUtils
import com.geospock.MockitoExtensions.any
import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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
        val halfAimMock = Mockito.mock(HalfAim::class.java)
        Mockito.`when`(halfAimMock.isLegal(any(), any(), any()))
                .thenReturn(false)

        val x = 1


    }
}
