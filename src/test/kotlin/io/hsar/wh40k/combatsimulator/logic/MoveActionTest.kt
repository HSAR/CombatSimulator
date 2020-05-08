package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MoveActionTest {
    @Test
    fun testHalfMoveActionGetRange() {
        assertThat(11, equalTo(ActionOption.HalfMove.getMovementRange(11)))
    }
    @Test
        fun testHalfActionIsValidMovementPath() {
        assertThat(true, equalTo(ActionOption.HalfMove.isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveActionGetRange() {
        assertThat(33, equalTo(ActionOption.ChargeAttack("1d5").getMovementRange(11)))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenFarEnough() {
        assertThat(true, equalTo(ActionOption.ChargeAttack("1d5").isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenTooClose() {
        assertThat(false, equalTo(ActionOption.ChargeAttack("1d5").isValidMovementPath(MapPosition(2,3), MapPosition(5, 5))))
    }

}