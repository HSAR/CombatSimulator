import io.hsar.wh40k.combatsimulator.model.ChargeAction
import io.hsar.wh40k.combatsimulator.model.HalfMoveAction
import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TestMoveAction {
    @Test
    fun testHalfMoveActionGetRange() {
        assertThat(11, equalTo(HalfMoveAction.getMovementRange(11)))
    }
    @Test
        fun testHalfActionIsValidMovementPath() {
        assertThat(true, equalTo(HalfMoveAction.isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveActionGetRange() {
        assertThat(33, equalTo(ChargeAction.getMovementRange(11)))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenFarEnough() {
        assertThat(true, equalTo(ChargeAction.isValidMovementPath(MapPosition(2,3), MapPosition(20, 20))))
    }
    @Test
    fun testChargeMoveIsValidMovementPathWhenTooClose() {
        assertThat(false, equalTo(ChargeAction.isValidMovementPath(MapPosition(2,3), MapPosition(5, 5))))
    }

}