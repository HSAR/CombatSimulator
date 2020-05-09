package io.hsar.wh40k.combatsimulator.model

import TestUtils
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.logic.HalfMove
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class WorldTest {

    @Test
    fun testCanMoveToUnitInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMove), equalTo(true))
    }

    @Test
    fun testCanMoveToUnitOutOfRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(6, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMove), equalTo(false))
    }

    @Test
    fun testDistanceApart() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(10, 4))
        assertThat(world.distanceApart(world.friendlyForces[0], world.enemyForces[0]), equalTo(9))
    }

    @Test
    fun testMapPositionDistancetoPosition() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first.distanceToPosition(second), equalTo(9))
    }

    @Test()
    fun testMapPositionOperatorMinus() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first - second, equalTo(9))
    }
}
