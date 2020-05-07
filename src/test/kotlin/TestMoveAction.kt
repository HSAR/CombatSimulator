import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TestMoveAction {
    @Test
    fun testHalfMoveActionGetRange() {
        assertThat(11, equalTo(TurnAction.HalfMoveAction.getMovementRange(11)))
    }
    @Test
        fun testHalfActionIsValidMovementPath() {
        assertThat(true, equalTo(TurnAction.HalfMoveAction.isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveActionGetRange() {
        assertThat(33, equalTo(TurnAction.ChargeAction.getMovementRange(11)))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenFarEnough() {
        assertThat(true, equalTo(TurnAction.ChargeAction.isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenTooClose() {
        assertThat(false, equalTo(TurnAction.ChargeAction.isValidMovementPath(MapPosition(2,3), MapPosition(5, 5))))
    }

}