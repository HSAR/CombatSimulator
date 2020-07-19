package io.hsar.wh40k.combatsimulator.logic

import TestUtils
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.ArgumentMatchers.any

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
